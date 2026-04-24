package org.lazberry.xmaslegacy;

import org.lazberry.xmaslegacy.Roles.Roles;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private final Map<UUID, User> users = new ConcurrentHashMap<>();

	public UserManager() {
	}

	public void addUser(User user) {
        users.put(user.getUUID(), user);
    }
    public void removeUser(UUID p) {
        users.remove(p);
    }
    public User getUser(UUID p) {
	    return users.get(p);
    }
	public List<User> getAllUsers() {
		return new ArrayList<>(users.values());
	}
    public boolean withdraw(UUID p, int amount) {
        User user = getUser(p);
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
	public Roles getRoleByUUID(UUID uuid) {
		if (users.containsKey(uuid)) {
			return users.get(uuid).getRole();
		}

		return null;
	}
}
