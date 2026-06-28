package xmaslegacy;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.User.UserManager;

@Slf4j
public class UserSaveManager {
	private static volatile @Nullable BukkitTask task;

	public static void startTask(@NotNull XmasLegacy plugin) {
		if (task != null) return;
		synchronized (UserSaveManager.class) {
			if (task != null) return;
			task = Bukkit.getScheduler()
					.runTaskTimerAsynchronously(plugin, UserManager.INSTANCE::saveAll,
							0L, Constants.USER_SAVE_TASK_DURATION);
			log.info("User save task started! ({} tick duration)", Constants.USER_SAVE_TASK_DURATION);
		}
	}

	public static void stopTask() {
		BukkitTask currentTask;

		synchronized (UserSaveManager.class) {
			currentTask = task;
			task = null;
		}
		if (currentTask != null) {
			currentTask.cancel();
			log.info("User save task Stopped.");
		}
	}
}
