package org.lazberry.xmaslegacy.api;

import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.DatabaseProcessor;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.ConfigValue;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
                "receiver VARCHAR(36) NOT NULL," +
                "product VARCHAR(20) NOT NULL," +
                "granted BOOLEAN DEFAULT FALSE" +
                ");";
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.execute();
            }
        } catch (SQLException e) {
            log.error("Error occurred while Creating Table Task -> {}", e.getMessage(), e);
        }
    }

    public CompletableFuture<Void> saveProduct(WebProduct webProduct, boolean granted) {
        synchronized (this) {
            return CompletableFuture.runAsync(() -> {
                var receiver = webProduct.getReceiver();
                var product = webProduct.getProduct();

                String query = "REPLACE INTO products (receiver, product, granted)" +
                        " VALUES (?, ?, ?);";

                try (Connection conn = getConnection();
                     PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                    preparedStatement.setString(1, receiver.toString());
                    preparedStatement.setString(2, product.name());
                    preparedStatement.setBoolean(3, granted);

                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    log.error("sql error while saving product -> {}", e.getMessage(), e);
                }
            });
        }
    }

    public CompletableFuture<List<WebProduct>> getUngrantedProducts(UUID receiver) {
        return CompletableFuture.supplyAsync(() -> {
            List<WebProduct> list = new ArrayList<>();
            String query = "SELECT receiver, product, granted FROM products WHERE receiver = ? AND granted = FALSE;";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setString(1, receiver.toString());
                try (var rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        WebProduct product = new WebProduct(
                                UUID.fromString(rs.getString("receiver")),
                                ProductType.valueOf(rs.getString("product"))
                        );
                        product.setGranted(rs.getBoolean("granted"));
                        list.add(product);
                    }
                }
            } catch (SQLException e) {
                log.error("sql error while fetching ungranted products -> {}", e.getMessage(), e);
            }
            return list;
        });
    }

    public CompletableFuture<Void> markAsGranted(WebProduct webProduct) {
        return CompletableFuture.runAsync(() -> {
            String query = "UPDATE products SET granted = TRUE WHERE receiver = ? AND product = ? AND granted = FALSE;";

            try (Connection conn = getConnection();
                 PreparedStatement preparedStatement = conn.prepareStatement(query)) {

                preparedStatement.setString(1, webProduct.getReceiver().toString());
                preparedStatement.setString(2, webProduct.getProduct().name());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                log.error("sql error while updating product grant status -> {}", e.getMessage(), e);
            }

        });
    }

    public CompletableFuture<List<WebProduct>> getAllUngrantedProducts() {
        return CompletableFuture.supplyAsync(() -> {
            List<WebProduct> list = new ArrayList<>();
            String query = "SELECT receiver, product, granted FROM products WHERE granted = FALSE;";

            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query);
                 var rs = stmt.executeQuery()) {

                while (rs.next()) {
                    WebProduct product = new WebProduct(
                            UUID.fromString(rs.getString("receiver")),
                            ProductType.valueOf(rs.getString("product"))
                    );
                    product.setGranted(rs.getBoolean("granted"));
                    list.add(product);
                }
            } catch (SQLException e) {
                log.error("sql error while fetching all ungranted products -> {}", e.getMessage(), e);
            }
            return list;
        });
    }
}
