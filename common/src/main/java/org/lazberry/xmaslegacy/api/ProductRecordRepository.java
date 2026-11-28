package org.lazberry.xmaslegacy.api;

import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.DatabaseProcessor;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.ConfigValue;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class ProductRecordRepository extends DatabaseProcessor {

    @Inject
    public ProductRecordRepository(@ConfigValue(path = "web.database.url") String url,
                                   @ConfigValue(path = "web.database.user") String user,
                                   @ConfigValue(path = "web.database.password") String password) {
        super(url, user, password);
        createTable();
    }

    @Override
    public void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS products(" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "receiver VARCHAR(36) NOT NULL," +
                "product VARCHAR(20) NOT NULL," +
                "granted BOOLEAN DEFAULT FALSE," +
                "INDEX idx_receiver (receiver)" +
                ");";
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.execute();
            }
            // One row per web order id, so a retried donation command maps back to the same purchase.
            try (PreparedStatement stmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS product_orders(" +
                    "order_id VARCHAR(64) PRIMARY KEY," +
                    "product_id BIGINT NOT NULL UNIQUE," +
                    "receiver VARCHAR(36) NOT NULL," +
                    "product VARCHAR(20) NOT NULL" +
                    ");")) {
                stmt.execute();
            }
        } catch (SQLException e) {
            log.error("Error occurred while Creating Table Task -> {}", e.getMessage(), e);
        }
    }

    /**
     * Inserts a new ungranted purchase.
     * @return the purchase number issued by the database. Fails exceptionally when the insert fails,
     * so a purchase never exists without an id.
     */
    public CompletableFuture<Long> saveProduct(UUID receiver, ProductType product) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "INSERT INTO products (receiver, product, granted) VALUES (?, ?, FALSE);";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, receiver.toString());
                stmt.setString(2, product.name());
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) return keys.getLong(1);
                    throw new SQLException("No purchase id generated.");
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Inserts a purchase for a web order, or returns the purchase already made for that order.
     * The purchase and the order row are written in one transaction, so a retry never creates a second purchase.
     * Fails if the order id is already tied to a different receiver or product.
     */
    public CompletableFuture<Long> saveOrderProduct(UUID receiver, ProductType product, String orderId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = getConnection()) {
                long existing = findOrderPurchase(conn, orderId, receiver, product);
                if (existing > 0) return existing;

                conn.setAutoCommit(false);
                try {
                    long purchaseId;
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO products (receiver, product, granted) VALUES (?, ?, FALSE);",
                            Statement.RETURN_GENERATED_KEYS)) {
                        stmt.setString(1, receiver.toString());
                        stmt.setString(2, product.name());
                        stmt.executeUpdate();
                        try (ResultSet keys = stmt.getGeneratedKeys()) {
                            if (!keys.next()) throw new SQLException("No purchase id generated.");
                            purchaseId = keys.getLong(1);
                        }
                    }
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO product_orders (order_id, product_id, receiver, product) VALUES (?, ?, ?, ?);")) {
                        stmt.setString(1, orderId);
                        stmt.setLong(2, purchaseId);
                        stmt.setString(3, receiver.toString());
                        stmt.setString(4, product.name());
                        stmt.executeUpdate();
                    }
                    conn.commit();
                    return purchaseId;
                } catch (SQLException e) {
                    conn.rollback();
                    // 1062 = duplicate key: the same order was saved concurrently; use that purchase.
                    if (e.getErrorCode() == 1062) {
                        conn.setAutoCommit(true);
                        long winner = findOrderPurchase(conn, orderId, receiver, product);
                        if (winner > 0) return winner;
                    }
                    throw e;
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    private static long findOrderPurchase(Connection conn, String orderId, UUID receiver, ProductType product) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT product_id, receiver, product FROM product_orders WHERE order_id = ?;")) {
            stmt.setString(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return 0;
                if (!receiver.toString().equalsIgnoreCase(rs.getString("receiver")) || !product.name().equals(rs.getString("product"))) {
                    throw new SQLException("Order " + orderId + " is already tied to a different receiver or product.");
                }
                return rs.getLong("product_id");
            }
        }
    }

    /** @return the ungranted purchase with this id, or null if it doesn't exist or is already granted. */
    public CompletableFuture<WebProduct> getUngrantedProduct(long id) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "SELECT id, receiver, product FROM products WHERE id = ? AND granted = FALSE;";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? read(rs) : null;
                }
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Flips the granted flag only if it currently has the opposite value.
     * @return true if this call changed the row. With granted = true this is an atomic claim:
     * only one caller across the whole network gets true for the same purchase.
     */
    public CompletableFuture<Boolean> setGranted(long id, boolean granted) {
        return CompletableFuture.supplyAsync(() -> {
            String query = "UPDATE products SET granted = ? WHERE id = ? AND granted = ?;";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setBoolean(1, granted);
                stmt.setLong(2, id);
                stmt.setBoolean(3, !granted);
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    public CompletableFuture<List<WebProduct>> getAllUngrantedProducts() {
        return CompletableFuture.supplyAsync(() -> {
            List<WebProduct> list = new ArrayList<>();
            String query = "SELECT id, receiver, product FROM products WHERE granted = FALSE;";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) list.add(read(rs));
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
            return list;
        });
    }

    private static WebProduct read(ResultSet rs) throws SQLException {
        return new WebProduct(
                rs.getLong("id"),
                UUID.fromString(rs.getString("receiver")),
                ProductType.valueOf(rs.getString("product"))
        );
    }
}
