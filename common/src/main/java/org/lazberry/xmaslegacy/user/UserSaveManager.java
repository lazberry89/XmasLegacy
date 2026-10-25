package org.lazberry.xmaslegacy.user;

import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Registry.Exclude(type = ServerType.LOBBY)
public class UserSaveManager {
    private static final Logger log = LoggerFactory.getLogger(UserSaveManager.class);

    private final UserManager userManager;
    private final SqlUserRepository repository;
    private final UserEmergencyDump dump;

	@Inject
    public UserSaveManager(UserManager userManager, SqlUserRepository repository, UserEmergencyDump dump) {
		this.userManager = userManager;
		this.repository = repository;
		this.dump = dump;
    }

	public void clearCache() {
		userManager.clearCache();
	}

	private @NotNull User loadOrCreateUser(UUID uuid, String name, boolean isFloodgate) {
		User loaded = dump.checkLocalEmergencyFile(uuid);
		boolean restoredFromDump = false;

		if (loaded != null) {
			restoredFromDump = true;
			log.info("User {} recovered from emergency dump!", uuid);
		} else {
			String cached = null;
			try {
				cached = JedisService.getFromJedis("user:" + uuid);
			} catch (Exception e) {
				log.warn("Redis lookup failed for user {}: {}. Falling back to database.", uuid, e.getMessage());
			}
			if (cached != null) {
				try {
					loaded = User.fromJson(cached);
				} catch (Exception e) {
					log.warn("Failed to parse user cache JSON for {}: {}", uuid, e.getMessage());
				}
			}
			if (loaded == null) {
				loaded = repository.loadUser(uuid);
			}
		}

		if (loaded == null) {
			loaded = new User(uuid, ServerRoles.USER, name);
			loaded.setNewUser(true);
			loaded.setMobile(isFloodgate);
			if (isFloodgate) loaded.addDollars(5000);
			else loaded.addDollars(2500);

			synchronized (loaded.getLock()) {
				try {
					repository.saveUser(loaded);
					JedisService.setToJedis("user:" + uuid, loaded.toJson());
				} catch (Exception e) {
					log.error("Failed to save new user {}", uuid, e);
					dump.threadDump(loaded);
				}
			}
		} else {
			loaded.setMobile(isFloodgate);
			if (restoredFromDump) {
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
		}

		return loaded;
	}

    public void saveAll() {
		userManager.getUsers().forEach(user -> {
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
	public CompletableFuture<User> onJoinAsync(UUID uuid, String name, String server, boolean isFloodgate) {
		return CompletableFuture.supplyAsync(() -> {
			User loaded = loadOrCreateUser(uuid, name, isFloodgate);
			userManager.addUser(loaded);
			JedisService.setUserServer(uuid, server);
			return loaded;
		});
	}

	@Blocking
	@Contract("_, _, _ -> !null")
	public User load(UUID uuid, String name, boolean isFloodgate) {
		User loaded = loadOrCreateUser(uuid, name, isFloodgate);
		userManager.addUser(loaded);
		return loaded;
	}

    @NonBlocking
    @Contract("_ -> new")
    public CompletableFuture<Void> onQuitAsync(UUID uuid) {
        User u = userManager.removeUser(uuid);
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
