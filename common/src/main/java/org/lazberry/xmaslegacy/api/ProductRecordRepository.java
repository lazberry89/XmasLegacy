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
