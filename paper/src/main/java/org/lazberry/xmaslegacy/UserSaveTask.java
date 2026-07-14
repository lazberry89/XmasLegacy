package org.lazberry.xmaslegacy;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.ServerType;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.User.UserSaveManager;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Task(type = ServerType.GLOBAL)
public enum UserSaveTask implements Tasks {
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
		synchronized (UserSaveTask.class) {
			if (task != null) return;

			this.task = Bukkit.getScheduler()
					.runTaskTimerAsynchronously(plugin, () -> {
								UserSaveManager.INSTANCE.saveAll();
								log.warn("User info saved automatically saved for {} tick duration.", Constants.USER_SAVE_TASK_DURATION);
							},
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
		synchronized (UserSaveTask.class) {
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

	public void loadValidUsers() {
		var us = UserSaveManager.INSTANCE;

		Map<UUID, String> playerData = Bukkit.getOnlinePlayers().stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(Player::getUniqueId, Player::getName));

		CompletableFuture.runAsync(() -> playerData.forEach(us::load));
	}
}
