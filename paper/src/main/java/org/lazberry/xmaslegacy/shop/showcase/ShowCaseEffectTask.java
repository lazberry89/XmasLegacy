package org.lazberry.xmaslegacy.shop.showcase;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Task
@Slf4j
@Registry.Exclude(type = ServerType.WILD)
public class ShowCaseEffectTask implements Tasks {
	private final ShowCaseManager showcaseManager;
	private volatile BukkitTask task;

	@Inject
	public ShowCaseEffectTask(ShowCaseManager showcaseManager) {
		this.showcaseManager = showcaseManager;
	}

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		if (task == null) synchronized (this) {
			if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, this::process, 0L, 2 * 20L);
		}
	}

	private void process() {
		int size = 0;

		for (var s : showcaseManager.getShowCases()) {
			var uuid = s.getSpawnedItem();
			if (uuid == null) continue;

			Entity entity = Bukkit.getEntity(uuid);

			if (entity != null && entity.isValid()) {
				var loc = entity.getLocation();
				loc.getWorld().spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, loc.add(0, 0.25, 0), 5, 0.15, 0.15, 0.15, 0.01);
			}
			size++;
		}
	}

	@Override
	public void stopTask() {
		if (task != null) synchronized (this) {
			if (task != null) {
				task.cancel();
				task = null;
			}
		}
	}
}
