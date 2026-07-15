package org.lazberry.xmaslegacy.User;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerManager;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Registry
public enum UserManager implements ServerManager {
	INSTANCE;

    private final @NotNull Map<UUID, User> users = new ConcurrentHashMap<>();
	private final @NotNull @Getter File rootDataFolder = new File("plugins/XmasLegacy");

	UserManager() {}

	public void init() {}

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
