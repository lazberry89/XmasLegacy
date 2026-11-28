package org.lazberry.xmaslegacy.catalog.database;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Task;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.plugin.Tasks;

@Task
@Components.Exclude(type = ServerType.LOBBY)
public class CatalogFlushTask implements Tasks {
	private static final long PERIOD = 20L * 60 * 5;

	private final CatalogSaveManager saveManager;
	private BukkitTask task;

	@Inject
	public CatalogFlushTask(CatalogSaveManager saveManager) {
		this.saveManager = saveManager;
	}

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		if (task == null) task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, saveManager::flush, PERIOD, PERIOD);
	}

	@Override
	public void stopTask() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}
}
