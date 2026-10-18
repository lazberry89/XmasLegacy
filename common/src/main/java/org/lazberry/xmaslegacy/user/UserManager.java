package org.lazberry.xmaslegacy.user;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.roles.Role;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.lazberry.xmaslegacy.roles.ServerRoles.USER;

@Registry.Exclude(type = ServerType.LOBBY)
public class UserManager {
    private final Map<UUID, User> users = new ConcurrentHashMap<>();
	private final @Getter File rootDataFolder;

	@Inject
	public UserManager(File rootDataFolder) {
		this.rootDataFolder = rootDataFolder;
	}

	public Optional<User> getOptionalUser(UUID uuid) {
		return Optional.ofNullable(getUser(uuid));
	}

	public void clearCache() {
		users.clear();
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
	public List<User> getUsers() {
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

    public void deposit(UUID uuid, int amount) {
        User user = getUser(uuid);
        if (user != null) {
            user.setDollars(user.getDollars() + amount);
        }
    }
	public Role getRole(UUID uuid) {
		if (users.containsKey(uuid)) {
			return users.get(uuid).getRole();
		}
		return USER;
	}

	public boolean startRole(UUID uuid, ServerRoles role) {
		User user = getUser(uuid);
		if (user == null) return false;

		Role getRole = user.getRole();
		if (USER.equals(getRole)) {
			user.setRole(role);
			return true;
		}
		return false;
	}
}
