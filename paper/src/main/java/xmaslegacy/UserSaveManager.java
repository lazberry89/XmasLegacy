package xmaslegacy;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Annotation.Task;
import xmaslegacy.PluginUtils.ServerType;
import xmaslegacy.PluginUtils.Tasks;

@Slf4j
@Task(type = ServerType.GLOBAL)
public enum UserSaveManager implements Tasks {
	INSTANCE;

	private volatile @Nullable BukkitTask task;

	/**
	 * Async scheduler will start. Also synchronized.
	 * @param plugin Plugin instance.
	 */
	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		if (task != null) return;
		// Synchronizing with using class lock.
		synchronized (UserSaveManager.class) {
			if (task != null) return;

			this.task = Bukkit.getScheduler()
					.runTaskTimerAsynchronously(plugin, UserManager.INSTANCE::saveAll,
							0L, Constants.USER_SAVE_TASK_DURATION);
			log.info("User save task started! ({} tick duration)", Constants.USER_SAVE_TASK_DURATION);
		}
	}

	/**
	 * Used local variable to hide Intellij Warning,
	 * Also catching volatile field problem.
	 */
	@Override
	public void stopTask() {
		// Localized variable
		BukkitTask currentTask;

		// Synchronized with current class
		synchronized (UserSaveManager.class) {
			currentTask = task;
			// make static field NULL first
			task = null;
		}
		// Used local variable for Checking
		if (currentTask != null) {
			currentTask.cancel();
			log.info("User save task Stopped.");
		}
	}
}
