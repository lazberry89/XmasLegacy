package xmaslegacy;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.User.UserManager;

public enum UserSaveManager {
	INSTANCE;

	private final @NotNull UserManager um;
	private @Nullable BukkitTask task;

	UserSaveManager() {
		this.um = UserManager.INSTANCE;
	}


}
