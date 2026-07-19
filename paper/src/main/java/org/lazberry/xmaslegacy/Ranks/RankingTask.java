package org.lazberry.xmaslegacy.Ranks;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.User.RankType;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Inject
@Task(type = ServerType.GLOBAL)
public enum RankingTask implements Tasks {
	INSTANCE;

	private final @NotNull Map<RankType, BukkitTask> tasks = new ConcurrentHashMap<>();
	private @NotNull RankManager rm;

    RankingTask() {}

	private @NotNull BukkitTask rankTask(@NotNull XmasLegacy plugin, @NotNull RankType type) {
		var task = tasks.get(type);
		if (task != null) return task;

		log.warn("Ranking task (TYPE : {}) started.", type);

		var newTask = Bukkit.getScheduler().runTaskTimer(plugin, () ->
			rm.getRank(type, Math.max(100, 0)).whenComplete((lst, ex) -> {
				if (ex != null) {
					log.error("Task occurred error while starting rank task. (TYPE : {})", type);
					return;
				}
				switch (type) {
					case DOLLAR -> rm.replaceDollarRank(lst);
					case EXP -> rm.replaceExpRank(lst);
					case ROLE_EXP -> rm.replaceRoleExpRank(lst);
					case PLAYTIME -> rm.replacePlaytime(lst);
				}
			}), 0L, 20L);
		this.tasks.put(type, newTask);
		return newTask;
	}


	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		Arrays.stream(RankType.values()).forEach(r -> {
			var task = this.rankTask(plugin, r);
			this.tasks.put(r, task);
		});
	}

	@Override
	public void stopTask() {
		this.tasks.keySet().forEach(this::stopRankTask);
		log.warn("Stopping all rank task.");
	}

	public void startRankTask(@NotNull XmasLegacy plugin, @NotNull RankType type) {
		var task = rankTask(plugin, type);
		this.tasks.put(type, task);
	}

	public void stopRankTask(@NotNull RankType type) {
		var task = tasks.get(type);
		if (task != null) task.cancel();
		tasks.remove(type);
	}
}
