package org.lazberry.xmaslegacy.User;

import java.util.UUID;

public interface UserRepository {
	User loadUser(UUID uuid);

	void saveUser(User user);

	int getRank(UUID uuid);

	boolean exist(UUID uuid);
}
