package org.lazberry.xmaslegacy;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.user.UserSaveManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Task
@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class UserSaveTask implements Tasks {
	private @Nullable BukkitTask task;
	private final UserSaveManager us;
	private final UserManager um;

	@Inject
	public UserSaveTask(@NotNull UserSaveManager us, UserManager um) {
		this.us = us;
        this.um = um;
    }

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		if (task != null) return;
		synchronized (this) {
			if (task != null) return;

			this.task = Bukkit.getScheduler()
					.runTaskTimer(plugin, () -> {
								us.saveAsyncAll();
								loadInvalidUsers();
								log.warn("User info automatically saved for {} tick duration.", Constants.USER_SAVE_TASK_DURATION);
							},
							0L, Constants.USER_SAVE_TASK_DURATION);
			log.info("User save task started! ({} tick duration)", Constants.USER_SAVE_TASK_DURATION);
		}
	}

	@Override
	public void stopTask() {
		if (task == null) return;
		task.cancel();
		task = null;
	}

	public void loadInvalidUsers() {
		Map<UUID, String> playerData = Bukkit.getOnlinePlayers().stream()
				.filter(p -> um.getOptionalUser(p.getUniqueId()).isEmpty())
				.collect(Collectors.toMap(Player::getUniqueId, Player::getName));

		CompletableFuture.runAsync(() -> playerData.forEach(us::load));
	}
}
