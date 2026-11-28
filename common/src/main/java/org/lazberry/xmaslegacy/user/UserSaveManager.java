package org.lazberry.xmaslegacy.user;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Components.Exclude(type = ServerType.LOBBY)
public class UserSaveManager {
    private static final Logger log = LoggerFactory.getLogger(UserSaveManager.class);

    private final UserManager userManager;
    private final SqlUserRepository repository;
    private final UserEmergencyDump dump;
    /** Server name each loaded player joined with. */
    private final Map<UUID, String> joinedServer = new ConcurrentHashMap<>();

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
			// Starting money is given once, on the first join (UserHandler, Constants.BASIC_MONEY_*).

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
			loaded.setName(name);
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
                    // An older dump would win over this save on the next join and roll the user back.
                    dump.deleteLocalEmergencyFile(user.getUniqueId());
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
			awaitPreviousServer(uuid, server);
			User loaded = loadOrCreateUser(uuid, name, isFloodgate);
			userManager.addUser(loaded);
			joinedServer.put(uuid, server);

			JedisService.setUserServer(uuid, server);
			JedisService.addOnlineUser(uuid);
			return loaded;
			}, Thread::startVirtualThread); // may sleep: keep the shared pool free
	}

	@Blocking
	@Contract("_, _, _ -> !null")
	public User load(UUID uuid, String name, boolean isFloodgate) {
		User loaded = loadOrCreateUser(uuid, name, isFloodgate);
		userManager.addUser(loaded);
		return loaded;
	}

    /**
     * When switching servers the proxy connects to the new server before the old one has saved.
     * Loading now would give the player their old data, so wait (up to 3s) until the old server signed off.
     */
    @Blocking
    public void awaitPreviousServer(UUID uuid, String server) {
    	try {
    		for (int i = 0; i < 60; i++) {
    			String previous = JedisService.getUserServer(uuid);
    			if (previous == null || previous.equals(server)) return;
    			Thread.sleep(50);
    		}
    		log.warn("{} was never signed off by its previous server (crash?). Loading the last saved data.", uuid);
    	} catch (InterruptedException e) {
    		Thread.currentThread().interrupt();
    	} catch (RuntimeException e) {
    		log.warn("Could not check the previous server of {}: {}", uuid, e.getMessage());
    	}
    }

    /** Loads only if nobody else did meanwhile; never replaces a user that is already in use. */
    @Blocking
    public void loadIfAbsent(UUID uuid, String name, boolean isFloodgate) {
    	if (userManager.getUser(uuid) == null) userManager.addUserIfAbsent(loadOrCreateUser(uuid, name, isFloodgate));
    }

    /** For a player who left before loading finished: forget the user without saving, and sign off. */
    public void discard(UUID uuid) {
    	userManager.removeUser(uuid);
    	CompletableFuture.runAsync(() -> signOff(uuid)).exceptionally(e -> {
    		log.warn("Failed to sign off {}", uuid, e);
    		return null;
    	});
    }

    /** Shutdown: players are not listed on this server any more. */
    public void signOffAll() {
    	Set.copyOf(joinedServer.keySet()).forEach(uuid -> {
    		try {
    			signOff(uuid);
    		} catch (RuntimeException e) {
    			log.warn("Failed to sign off {}", uuid, e);
    		}
    	});
    }

    /** Only if the network still lists the player here: a newer login on another server must stay. */
    private void signOff(UUID uuid) {
    	String server = joinedServer.remove(uuid);
    	String listed = JedisService.getUserServer(uuid);
    	if (listed != null && server != null && !listed.equals(server)) return;
    	JedisService.removeUserServer(uuid);
    	JedisService.removeOnlineUser(uuid);
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
					dump.deleteLocalEmergencyFile(uuid);
					log.info("User {} saved.", uuid);
                }
            } catch (Exception e) {
                	log.error("CRITICAL | User {} failed saving.", uuid, e);
                	dump.threadDump(u);
                } finally {
                	signOff(uuid); // even after a failed save, or the next server would wait for nothing
                }
        });
    }
}
