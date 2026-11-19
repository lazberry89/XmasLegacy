package org.lazberry.xmaslegacy.bags.database;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.bags.Bag;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.ConfigValue;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.InventorySerializer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class SqlBagRepository implements BagRepository {
    private static final String TABLE = "bags";

    private final String url;
    private final String user;
    private final String password;

	@Inject
    public SqlBagRepository(@ConfigValue(path = "web.database.url") String url,
	                        @ConfigValue(path = "web.database.user") String user,
	                        @ConfigValue(path = "web.database.password") String password) {
		this.url = url;
		this.user = user;
		this.password = password;
		createTable();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void createTable() {
        String query = "CREATE TABLE IF NOT EXISTS " + TABLE + " (" +
                "uuid VARCHAR(36) PRIMARY KEY," +
                "contents TEXT)";
        // Items for players who are not on the server giving them. Rows are only inserted by
        // anyone and only removed by the server the player is on, so the bag row never races.
        String mailbox = "CREATE TABLE IF NOT EXISTS bag_mailbox (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "uuid VARCHAR(36) NOT NULL," +
                "contents MEDIUMTEXT NOT NULL," +
                "INDEX idx_uuid (uuid))";
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             PreparedStatement mailboxStatement = connection.prepareStatement(mailbox)) {
            preparedStatement.execute();
            mailboxStatement.execute();
        } catch (SQLException e) {
            log.error("Failed to create table '{}'. If this exception is not successfully solved, " +
                    "entire user's bag will be extinguished.", TABLE, e);
        }
    }

    /** Appends items to the player's mailbox. Fails exceptionally so the caller can log what was lost. */
    public CompletableFuture<Void> addToMailbox(UUID uuid, ItemStack[] items) {
        String query = "INSERT INTO bag_mailbox (uuid, contents) VALUES (?, ?)";
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, uuid.toString());
                statement.setString(2, InventorySerializer.serializeContents(items));
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new CompletionException(e);
            }
        });
    }

    /**
     * Removes and returns everything in the player's mailbox in one transaction, so two servers
     * claiming at the same time can never both receive the same row.
     */
    public CompletableFuture<List<ItemStack>> claimMailbox(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                connection.setAutoCommit(false);
                try {
                    List<ItemStack> items = new ArrayList<>();
                    long maxId = -1;
                    try (PreparedStatement select = connection.prepareStatement(
                            "SELECT id, contents FROM bag_mailbox WHERE uuid = ? ORDER BY id FOR UPDATE")) {
                        select.setString(1, uuid.toString());
                        try (ResultSet rs = select.executeQuery()) {
                            while (rs.next()) {
                                maxId = rs.getLong("id");
                                for (ItemStack item : InventorySerializer.deserializeContents(rs.getString("contents"))) {
                                    if (item != null && !item.getType().isAir()) items.add(item);
                                }
                            }
                        }
                    }
                    if (maxId >= 0) {
                        try (PreparedStatement delete = connection.prepareStatement(
                                "DELETE FROM bag_mailbox WHERE uuid = ? AND id <= ?")) {
                            delete.setString(1, uuid.toString());
                            delete.setLong(2, maxId);
                            delete.executeUpdate();
                        }
                    }
                    connection.commit();
                    return items;
                } catch (Exception e) {
                    // A row that fails to deserialize rolls the whole claim back: nothing is deleted.
                    connection.rollback();
                    throw e;
                }
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
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
                    // 데이터베이스에 해당 UUID 레코드가 없는 경우 (신규 유저)
                    return null;
                }
            } catch (SQLException e) {
                log.error("Cannot load user({})'s bag from database.", uuid, e);
                throw new RuntimeException("Database error while loading bag for " + uuid, e);
            } catch (Exception e) {
                log.error("Exception occurred while loading user({})'s bag. It may be ItemStack array Serializing Exception or " +
                        "network unstable, or something.", uuid, e);
                throw new RuntimeException("Unexpected error while loading bag for " + uuid, e);
            }
        });
    }
}
