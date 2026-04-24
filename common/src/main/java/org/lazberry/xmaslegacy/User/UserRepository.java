package org.lazberry.xmaslegacy.User;

import java.util.UUID;

public interface UserRepository {
	User loadUser(UUID uuid);

	void saveUser(User user);

	boolean exist(UUID uuid);
}
