package org.lazberry.xmaslegacy.region.indicator;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.plugin.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.region.Region;
import org.lazberry.xmaslegacy.region.RegionManager;

import java.util.Collection;

@Task
@Registry.Exclude(type = ServerType.LOBBY)
public class IndicatorRecoveryTask implements Tasks {
    private final RegionManager regionManager;
    private final RegionDisplayManager displayManager;
    private BukkitTask task;

    @Inject
    public IndicatorRecoveryTask(RegionManager regionManager, RegionDisplayManager displayManager) {
        this.regionManager = regionManager;
        this.displayManager = displayManager;
    }

    @Override
    public void startTask(@NotNull XmasLegacy plugin) {
        if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            var regions = invalidRegions();
            if (regions.isEmpty()) return;

            regions.forEach(this::cleanUpAndCreate);
        }, 20L, 20 * 5L);
    }

    private Collection<Region> invalidRegions() {
        return regionManager.getRegions().stream()
                .filter(r -> r.getIndicator() == null || r.getInteraction() == null)
                .toList();
    }

    private void cleanUpAndCreate(Region region) {
        displayManager.removeDisplay(region);
        displayManager.spawnIndicatorDisplay(region);
    }

    @Override
    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
