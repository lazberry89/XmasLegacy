package org.lazberry.xmaslegacy.region.indicator;

import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.region.RegionManager;

@Task
@Registry.Exclude(type = ServerType.LOBBY)
public class IndicatorRotatorTask implements Tasks {
    private final RegionManager regionManager;
    private float globalAngle = 0.0f;
    private BukkitTask task;

    @Inject
    public IndicatorRotatorTask(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @Override
    public void startTask(@NotNull XmasLegacy plugin) {
        if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            var regions = regionManager.getRegions();
            if (regions.isEmpty()) return;

            Quaternionf angle = calculateQuaternion();

            regions.forEach(region -> {
                var display = region.getIndicator();
                if (display != null) setTrans(display, angle);
            });
        }, 20L, 3L);
    }

    private Quaternionf calculateQuaternion() {
        globalAngle += (float) Math.toRadians(3);
        if (globalAngle >= Math.PI * 2) globalAngle = 0.0f;
        return new Quaternionf(new AxisAngle4f(globalAngle, 0.0f, 1.0f, 0.0f));
    }

    private void setTrans(BlockDisplay indicator, Quaternionf leftRotation) {
        if (indicator != null && indicator.isValid()) {
            Transformation transformation = indicator.getTransformation();
            Transformation newTrans = new Transformation(
                    transformation.getTranslation(),
                    leftRotation,
                    transformation.getScale(),
                    transformation.getRightRotation()
            );
            indicator.setTransformation(newTrans);
        }
    }

    @Override
    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
