package org.lazberry.xmaslegacy.temperature;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.plugin.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.user.UserManager;

@Task
@Registry.Exclude(type = ServerType.LOBBY)
public class WarmthApplyTask implements Tasks {
    private final WarmthManager warmthManager;
    private final UserManager userManager;
    private BukkitTask task;

    @Inject
    public WarmthApplyTask(WarmthManager warmthManager, UserManager userManager) {
        this.warmthManager = warmthManager;
        this.userManager = userManager;
    }

    @Override
    public void startTask(@NotNull XmasLegacy plugin) {
        if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, () ->
            warmthManager.getZones()
                    .forEach(z -> z.applyWarmthEffect(userManager)), 20L, 60L);
    }

    @Override
    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
