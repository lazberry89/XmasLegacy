package org.lazberry.xmaslegacy.shop.showcase;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Task
@Registry.Exclude(type = ServerType.LOBBY)
public class ShowCaseForceLoader implements Tasks {
	private final ShowCaseManager showCaseManager;
	private BukkitTask task;

	@Inject
	public ShowCaseForceLoader(ShowCaseManager showCaseManager) {
		this.showCaseManager = showCaseManager;
	}

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		if (this.task == null) {
			task = Bukkit.getScheduler().runTaskTimer(plugin, () ->
				showCaseManager.getShowCases().forEach(s ->
					s.getBaseLocation().getChunk().load()), 0L, 20 * 10L);
		}
	}

	@Override
	public void stopTask() {

	}
}
