package org.lazberry.xmaslegacy.User;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface UserRepository {
	@Nullable User loadUser(@NotNull UUID uuid);

	void saveUser(@NotNull User user);

	int getRank(@NotNull UUID uuid);

	boolean exist(@NotNull UUID uuid);
}
