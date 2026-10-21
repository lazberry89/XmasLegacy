package org.lazberry.xmaslegacy.rank;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Task
@Registry.Exclude(type = ServerType.LOBBY)
public class RankTask implements Tasks {
    private final ProxyRankManager rankManager;
    private BukkitTask task;

    @Inject
    public RankTask(ProxyRankManager rankManager) {
        this.rankManager = rankManager;
    }

    @Override
    public void startTask(@NotNull XmasLegacy plugin) {
        if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            rankManager.updateRanks();
        }, 0L, 20L * 3);
    }

    @Override
    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
