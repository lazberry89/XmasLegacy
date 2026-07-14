package org.lazberry.xmaslegacy.User;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.RoleMastery;
import org.lazberry.xmaslegacy.settings.Tier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum UserManager {
	INSTANCE;

    private final @NotNull Map<UUID, User> users = new ConcurrentHashMap<>();
	private @NotNull @Getter File rootDataFolder = new File("plugins/XmasLegacy");
	private static final Logger log = LoggerFactory.getLogger(UserManager.class);

	UserManager() {}

	public void initDataFolder(@Nullable File dataFolder) {
		if (dataFolder != null)
			this.rootDataFolder = dataFolder;
	}

	public void addUser(@NotNull User user) {
        users.put(user.getUniqueId(), user);
    }
    public @Nullable User removeUser(@NotNull UUID uuid) {
		return users.remove(uuid);
	}
    public @Nullable User getUser(@NotNull UUID uuid) {
	    return users.get(uuid);
    }
	@Contract("-> new")
	public @NotNull List<User> getUsers() {
		return new ArrayList<>(users.values());
	}
    public boolean withdraw(@NotNull UUID uuid, int amount) {
        User user = getUser(uuid);
        if (user != null && user.getDollars() >= amount) {
            user.setDollars(user.getDollars() - amount);
            return true;
        }
        return false;
    }

    public void deposit(@NotNull UUID uuid, int amount) {
        User user = getUser(uuid);
        if (user != null) {
            user.setDollars(user.getDollars() + amount);
        }
    }
	public @Nullable Role getRole(@NotNull UUID uuid) {
		if (users.containsKey(uuid)) {
			return users.get(uuid).getRole();
		}

		return null;
	}

	public boolean startRole(@NotNull UUID uuid, @NotNull BasicRoles role) {
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
