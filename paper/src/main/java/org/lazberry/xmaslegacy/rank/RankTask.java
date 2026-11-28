package org.lazberry.xmaslegacy.rank;

import org.lazberry.xmaslegacy.rank.board.RankBoardManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Task;
import org.lazberry.xmaslegacy.plugin.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;

@Task
@Components.Exclude(type = ServerType.LOBBY)
public class RankTask implements Tasks {
    private final ProxyRankManager rankManager;
    private final RankBoardManager boardManager;
    private BukkitTask task;

    @Inject
    public RankTask(ProxyRankManager rankManager, RankBoardManager boardManager) {
        this.rankManager = rankManager;
        this.boardManager = boardManager;
    }

    @Override
    public void startTask(@NotNull XmasLegacy plugin) {
        if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			rankManager.updateAll();
            rankManager.updateRanks();
            boardManager.updateAll(); // draws the lists of the previous run; updateRanks() is async
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
