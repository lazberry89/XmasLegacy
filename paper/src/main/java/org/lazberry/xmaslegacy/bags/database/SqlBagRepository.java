package org.lazberry.xmaslegacy.bags.database;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.bags.Bag;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InventorySerializer;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Exclude(type = ServerType.GLOBAL)
public class SqlBagRepository implements BagRepository {
    private static final String TABLE = "bags";

    private final String url = "jdbc:mysql://localhost:3306/xmas_legacy?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private final String user = "husksync";
    private final String password = "husksync";

    public SqlBagRepository() {
        createTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "contents TEXT)";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);) {
            preparedStatement.execute();
        } catch (SQLException e) {
            log.error("Failed to create table '{}'. If this exception is not successfully solved, " +
                    "entire user's bag will be extinguished.", TABLE, e);
        }
    }

    @Override
    public CompletableFuture<Boolean> save(Bag bag) {
        String query = "INSERT INTO bags (uuid, contents) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE contents = ?";
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                String serialized = InventorySerializer.serializeContents(bag.getContents());

                preparedStatement.setString(1, bag.getOwner().toString());
                preparedStatement.setString(2, serialized);
                preparedStatement.setString(3, serialized);
                preparedStatement.executeUpdate();
                return true;
            } catch (SQLException e) {
                log.error("Failed to save user({})'s bag to database.", bag.getOwner(), e);
                return false;
            }
        });
    }

    @Override
    public CompletableFuture<Bag> load(UUID uuid) {
        String query = "SELECT * FROM " + TABLE + " WHERE uuid = ? LIMIT 1";
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, uuid.toString());

                try (ResultSet result = preparedStatement.executeQuery()) {
                    if (result.next()) {
                        ItemStack[] contents = InventorySerializer.deserializeContents(
                                result.getString("contents"));
                        return new Bag(uuid, contents);
                    }
                }
            } catch (SQLException e) {
                log.error("Cannot load user({})'s bag from database.", uuid, e);
            } catch (Exception e) {
                log.error("Exception occurred while loading user({})'s bag. It may be ItemStack array Serializing Exception or" +
                        "network unstable, or something", uuid, e);
            }
            return new Bag(uuid);
        });
    }
}
