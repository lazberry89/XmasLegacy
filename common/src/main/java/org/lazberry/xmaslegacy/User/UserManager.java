package org.lazberry.xmaslegacy.User;

import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.settings.RoleMastery;
import org.lazberry.xmaslegacy.settings.Tier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public enum UserManager {
	INSTANCE;

    private final @NotNull Map<UUID, User> users = new ConcurrentHashMap<>();
	private final @NotNull UserRepository repository = SqlUserRepository.INSTANCE;
	private @NotNull File rootDataFolder = new File("plugins/XmasLegacy");
	private static final @NotNull Logger logger = LoggerFactory.getLogger(UserManager.class);

	UserManager() {}

	public void initDataFolder(File dataFolder) {
		if (dataFolder != null) {
			this.rootDataFolder = dataFolder;
		}
	}

	public void addUser(@NotNull User user) {
        users.put(user.getUUID(), user);
    }
    public void removeUser(UUID uuid) {users.remove(uuid);}
    public @Nullable User getUser(@NotNull UUID uuid) {
	    return users.get(uuid);
    }
	public List<User> getAllUsers() {
		return new ArrayList<>(users.values());
	}
    public boolean withdraw(UUID uuid, int amount) {
        User user = getUser(uuid);
        if (user != null && user.getDollars() >= amount) {
            user.setDollars(user.getDollars() - amount);
            return true;
        }
        return false;
    }

    public void deposit(UUID p, int amount) {
        User user = getUser(p);
        if (user != null) {
            user.setDollars(user.getDollars() + amount);
        }
    }
	public Role getRole(UUID uuid) {
		if (users.containsKey(uuid)) {
			return users.get(uuid).getRole();
		}

		return null;
	}

	/**
	 * @param uuid uuid
	 * @param name name
	 * @return User 불러온 유저, 없으면 새로 생성하여 반환
	 */
	@Blocking
	public User load(UUID uuid, String name) {
		User loaded = repository.loadUser(uuid);
		if (loaded == null) {
			loaded = new User(uuid, BasicRoles.USER, name);
			loaded.setNewUser(true);
			repository.saveUser(loaded);
		}
		users.put(uuid, loaded);
		return loaded;
	}

	@NonBlocking
	public CompletableFuture<User> onJoinAsync(UUID uuid, String name, boolean isFloodgate) {
		return CompletableFuture.supplyAsync(() -> {
			User loaded = checkLocalEmergencyFile(uuid);
			boolean restoredFromDump = false;

			if (loaded != null) {
				restoredFromDump = true;
				logger.info("User {} recovered from emergency dump!", uuid);
			} else {
				loaded = repository.loadUser(uuid);
			}

			if (loaded == null) {
				loaded = new User(uuid, BasicRoles.USER, name);
				loaded.setNewUser(true);

				if (isFloodgate) {
					loaded.setDollars(loaded.getDollars() + 5000);
				}
				repository.saveUser(loaded);
			} else if (restoredFromDump) {
				try {
					repository.saveUser(loaded);
					deleteLocalEmergencyFile(uuid);
				} catch (Exception e) {
					logger.error("Failed to sync restored user {} back to DB.", uuid, e);
				}
			}
			this.addUser(loaded);
			//users.put(uuid, loaded);
			return loaded;
		});
	}

	@NonBlocking
	public CompletableFuture<Void> onQuitAsync(UUID uuid) {
		User u = users.remove(uuid);
		if (u == null) return CompletableFuture.completedFuture(null);

		return CompletableFuture.runAsync(() -> {
			try {
				repository.saveUser(u);
				logger.info("User {} saved.", uuid);
			} catch (Exception e) {
				logger.error("CRITICAL | User {} failed saving.", uuid, e);
				threadDump(u);
			}
		});
	}

	private void threadDump(@NotNull User user) {
		File dumpDir = new File(rootDataFolder, "emergency_dumps");
		if (!dumpDir.exists())
			if (!dumpDir.mkdirs()) {
				logger.error("Failed to create emergency dump directory. User {}'s info may got lost.", user.getUUID());
				return;
			}
		File dumpFile = new File(dumpDir, user.getUUID() + ".properties");

		Properties props = new Properties();
		props.setProperty("uuid", user.getUUID().toString());
		props.setProperty("name", user.getName());
		props.setProperty("role", user.getRole().name());
		props.setProperty("dollars", String.valueOf(user.getDollars()));
		props.setProperty("inquireCount", String.valueOf(user.getInquireCount()));
		props.setProperty("playTime", String.valueOf(user.getPlayTime()));
		props.setProperty("Exp", String.valueOf(user.getExp()));
		props.setProperty("roleExp", String.valueOf(user.getRoleExp()));
		props.setProperty("level", String.valueOf(user.getLevel()));
		props.setProperty("isNewUser", String.valueOf(user.isNewUser()));
		props.setProperty("wantsCookie", String.valueOf(user.ifWantsCookie()));
		props.setProperty("tier", user.getTier().name());
		props.setProperty("mastery", user.getMastery().name());
		props.setProperty("isImmuneToIcing", String.valueOf(user.isImmuneToIcing()));
		props.setProperty("icingState", String.valueOf(user.getIcingState()));

		try (FileOutputStream out = new FileOutputStream(dumpFile)) {
			props.store(out, "Emergency Backup for " + user.getName());
			logger.warn("User {}'s info got backUp. (Thread Dump via Properties)", user.getUUID());
		} catch (Exception e) {
			logger.error("Local backUp failed. Info may got lost.", e);
		}
	}

	private User checkLocalEmergencyFile(UUID uuid) {
		File dumpFile = new File(new File(rootDataFolder, "emergency_dumps"), uuid.toString() + ".properties");
		if (!dumpFile.exists()) return null;

		Properties props = new Properties();
		try (FileInputStream in = new FileInputStream(dumpFile)) {
			props.load(in);
			String name = props.getProperty("name", "Unknown");

			Role role;
			try {
				role = Role.valueOf(props.getProperty("role", "USER"));
			} catch (IllegalArgumentException e) {
				role = BasicRoles.USER;
				logger.warn("No valid role name of {}, replaced to Roles.USER.", name);
			}

			User recoveredUser = new User(uuid, role, name);

			recoveredUser.setDollars(Integer.parseInt(props.getProperty("dollars", "0")));
			recoveredUser.setInquireCount(Integer.parseInt(props.getProperty("inquireCount", "0")));
			recoveredUser.setPlayTime(Integer.parseInt(props.getProperty("playTime", "0")));
			recoveredUser.setExp(Double.parseDouble(props.getProperty("Exp", "0.0")));
			recoveredUser.setRoleExp(Double.parseDouble(props.getProperty("roleExp", "0.0")));
			recoveredUser.setLevel(Integer.parseInt(props.getProperty("level", "0")));
			recoveredUser.setNewUser(Boolean.parseBoolean(props.getProperty("isNewUser", "false")));
			recoveredUser.wantsCookie(Boolean.parseBoolean(props.getProperty("wantsCookie", "false")));

			recoveredUser.setTier(Tier.valueOf(props.getProperty("tier", "BRONZE")));
			recoveredUser.setMastery(RoleMastery.valueOf(props.getProperty("mastery", "BEGINNER")));
			recoveredUser.setImmuneToIcing(Boolean.parseBoolean(props.getProperty("isImmuneToIcing", "false")));
			recoveredUser.setIcingState(Integer.parseInt(props.getProperty("icingState", "100")));

			return recoveredUser;
		} catch (Exception e) {
			logger.error("Failed to load emergency dump for {}", uuid, e);
			return null;
		}
	}

	private void deleteLocalEmergencyFile(UUID uuid) {
		File dumpFile = new File(new File(rootDataFolder, "emergency_dumps"), uuid.toString() + ".properties");
		if (dumpFile.exists() && dumpFile.delete()) {
			logger.info("Cleaned up emergency dump for User {}", uuid);
		}
	}

	public boolean startRole(UUID uuid, BasicRoles role) {
		User user = getUser(uuid);
		if (user == null) return false;

		Role getRole = user.getRole();
		if (BasicRoles.USER.equals(getRole)) {
			user.setRole(role);
			return true;
		}
		return false;
	}
}
