package org.lazberry.xmaslegacy.User;

import org.jetbrains.annotations.CheckReturnValue;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.Roles;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private final Map<UUID, User> users = new ConcurrentHashMap<>();
	private final UserRepository repository = new SqlUserRepository();

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
	public Role getRoleByUUID(UUID uuid) {
		if (users.containsKey(uuid)) {
			return users.get(uuid).getRole();
		}

		return null;
	}

	/**
	 * !!비동기 권장!!
	 * @param uuid uuid
	 * @param name name
	 */
	public void load(UUID uuid, String name) {
		User loaded = repository.loadUser(uuid);
		if (loaded == null) {
			// 신규 유저 생성
			loaded = new User(uuid, Roles.USER, name);
			loaded.setNewUser(true);
			repository.saveUser(loaded);
		}
		users.put(uuid, loaded);
	}

	/**
	 * !!비동기 권장!!
	 * @param uuid uuid
	 */
	public void onQuit(UUID uuid) {
		User u = users.remove(uuid);
		if (u != null) {
			repository.saveUser(u);
		}
	}

	public CompletableFuture<User> onJoinAsync(UUID uuid, String name, boolean isFloodgate) {
		return CompletableFuture.supplyAsync(() -> {
			User loaded = repository.loadUser(uuid);

			if (loaded == null) {
				loaded = new User(uuid, Roles.USER, name);
				loaded.setNewUser(true);

				if (isFloodgate) {
					loaded.setDollars(loaded.getDollars() + 5000);
				}
				repository.saveUser(loaded);
			}

			users.put(uuid, loaded);
			return loaded;
		});
	}

	public void onQuitAsync(UUID uuid) {
		CompletableFuture.runAsync(() -> {
			User u = users.remove(uuid);
			if (u != null) {
				repository.saveUser(u);
			}
		});
	}

	public boolean startRole(UUID uuid, Roles role) {
		User user = getUser(uuid);
		if (user == null) return false;
		Role getRole = user.getRole();
		if (Roles.USER.equals(getRole)) {
			user.setRole(role);
			return true;
		}
		return false;
	}
}
