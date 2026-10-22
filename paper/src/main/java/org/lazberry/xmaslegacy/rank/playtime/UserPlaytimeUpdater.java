package org.lazberry.xmaslegacy.rank.playtime;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
public class UserPlaytimeUpdater implements Tasks {
    private final UserPlaytimeManager playtimeManager;
    private BukkitTask task;

    @Inject
    public UserPlaytimeUpdater(UserPlaytimeManager playtimeManager) {
        this.playtimeManager = playtimeManager;
    }

    @Override
    public void startTask(@NotNull XmasLegacy plugin) {
        if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, () ->
            Bukkit.getOnlinePlayers().stream()
                    .filter(Player::isValid)
                    .map(Player::getUniqueId)
                    .forEach(playtimeManager::updateUserPlaytime), 0L, 20 * 30L);
    }

    @Override
    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
