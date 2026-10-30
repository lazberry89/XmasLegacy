package org.lazberry.xmaslegacy.region.indicator;

import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.plugin.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.region.RegionManager;

@Task
@Registry.Exclude(type = ServerType.LOBBY)
public class IndicatorRotatorTask implements Tasks {
    private static final int PERIOD_TICKS = 2; // 0.1초마다 회전 (부드러운 보간 주기)
    private static final float ROTATION_PER_STEP = (float) Math.toRadians(7); // 1스텝당 7도 (초당 70도, 약 5.1초에 1바퀴)
    private static final float SCALE = 0.5f;

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
        }, 20L, PERIOD_TICKS);
    }

    private Quaternionf calculateQuaternion() {
        globalAngle += ROTATION_PER_STEP;
        if (globalAngle >= Math.PI * 2) {
            globalAngle -= (float) (Math.PI * 2);
        }
        return new Quaternionf(new AxisAngle4f(globalAngle, 0.0f, 1.0f, 0.0f));
    }

    private void setTrans(BlockDisplay indicator, Quaternionf leftRotation) {
        if (indicator != null && indicator.isValid()) {
            // BlockDisplay의 로컬 블록 원점은 모서리(0,0,0)입니다.
            // 중심 C(scale/2, scale/2, scale/2)를 기준으로 회전시키려면,
            // Translation = -(leftRotation * C) 로 동적 보정해야 피벗이 모서리가 아닌 블록의 정중앙에 고정됩니다.
            Vector3f center = new Vector3f(SCALE / 2f, SCALE / 2f, SCALE / 2f);
            Vector3f translation = new Vector3f();
            leftRotation.transform(center, translation).negate();

            Transformation newTrans = new Transformation(
                    translation,
                    leftRotation,
                    new Vector3f(SCALE, SCALE, SCALE),
                    new Quaternionf() // identity
            );

            // 클라이언트 사이드에서 끊김 없이 60fps로 부드럽게 보간(Interpolation)되도록 갱신
            indicator.setInterpolationDelay(0);
            indicator.setInterpolationDuration(PERIOD_TICKS);
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
