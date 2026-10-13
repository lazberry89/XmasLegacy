package org.lazberry.xmaslegacy.user;

import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Registry.Exclude(type = ServerType.LOBBY)
public class UserSaveManager implements Initiator {
    private static final @NotNull Logger log = LoggerFactory.getLogger(UserSaveManager.class);
    private final @NotNull UserManager um;
    private final @NotNull SqlUserRepository repository;
    private final @NotNull UserEmergencyDump dump;

	@Inject
    public UserSaveManager(@NotNull UserManager um, @NotNull SqlUserRepository repository, @NotNull UserEmergencyDump dump) {
		this.um = um;
		this.repository = repository;
		this.dump = dump;
    }

    @Override
    public void init() {

    }

	private @NotNull User loadOrCreateUser(@NotNull UUID uuid, @NotNull String name, boolean isFloodgate) {
		User loaded = dump.checkLocalEmergencyFile(uuid);
		boolean restoredFromDump = false;

		if (loaded != null) {
			restoredFromDump = true;
			log.info("User {} recovered from emergency dump!", uuid);
		} else {
			String cached = JedisService.getFromJedis("user:" + uuid);
			if (cached != null) {
				loaded = User.fromJson(cached);
			}
			if (loaded == null) {
				loaded = repository.loadUser(uuid);
			}
		}

		if (loaded == null) {
			loaded = new User(uuid, ServerRoles.USER, name);
			loaded.setNewUser(true);
			if (isFloodgate) loaded.addDollars(5000);

			synchronized (loaded.getLock()) {
				repository.saveUser(loaded);
				JedisService.setToJedis("user:" + uuid, loaded.toJson());
			}
		} else if (restoredFromDump) {
			try {
				synchronized (loaded.getLock()) {
					repository.saveUser(loaded);
					JedisService.setToJedis("user:" + uuid, loaded.toJson());
					dump.deleteLocalEmergencyFile(uuid);
				}
			} catch (Exception e) {
				log.error("Failed to sync restored user {} back to DB.", uuid, e);
			}
		}

		return loaded;
	}

    public void saveAll() {
        um.getUsers().forEach(user -> {
            synchronized (user.getLock()) {
                try {
                    repository.saveUser(user);
	                JedisService.setToJedis("user:" + user.getUniqueId(), user.toJson());
                } catch (Exception e) {
                    log.error("Failed to save user {}", user.getUniqueId(), e);
                    dump.threadDump(user);
                }
            }
        });
    }

    public CompletableFuture<Void> saveAsyncAll() {
        return CompletableFuture.runAsync(this::saveAll);
    }

	@NonBlocking
	@Contract("_, _, _, _ -> new")
	public @NotNull CompletableFuture<User> onJoinAsync(@NotNull UUID uuid, @NotNull String name, String server, boolean isFloodgate) {
		return CompletableFuture.supplyAsync(() -> {
			User loaded = loadOrCreateUser(uuid, name, isFloodgate);
			um.addUser(loaded);
			JedisService.setUserServer(uuid, server);
			return loaded;
		});
	}

	@Blocking
	@Contract("_, _ -> !null")
	public @NotNull User load(@NotNull UUID uuid, @NotNull String name) {
		User loaded = loadOrCreateUser(uuid, name, false);
		um.addUser(loaded);
		return loaded;
	}

    @NonBlocking
    @Contract("_ -> new")
    public @NotNull CompletableFuture<Void> onQuitAsync(@NotNull UUID uuid) {
        User u = um.removeUser(uuid);
        if (u == null) return CompletableFuture.completedFuture(null);

        return CompletableFuture.runAsync(() -> {
            try {
                synchronized (u.getLock()) {
                    repository.saveUser(u);
					JedisService.setToJedis("user:" + u.getUniqueId(), u.toJson());
					JedisService.removeUserServer(uuid);
                    log.info("User {} saved.", uuid);
                }
            } catch (Exception e) {
                log.error("CRITICAL | User {} failed saving.", uuid, e);
                dump.threadDump(u);
            }
        });
    }

}
