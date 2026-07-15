package org.lazberry.xmaslegacy.Region;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.lazberry.xmaslegacy.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Manager;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Map;

@Slf4j
@Inject
@Task(type = ServerType.GLOBAL)
public class RegionTaskManager implements Tasks {
	private @Manager @NotNull RegionManager rm;
    private @Nullable BukkitTask task;
    private float globalAngle = 0.0f;

    @Override
    public void startTask(@NotNull XmasLegacy plugin) {
        Map<Long, Region> regions = rm.getRegionMap();
        this.task = new BukkitRunnable() {
            int checkDelay = 0;

            @Override
            public void run() {
                if (regions.isEmpty()) return;

                checkDelay++;

                globalAngle += (float) Math.toRadians(3);
                if (globalAngle >= Math.PI * 2) globalAngle = 0.0f;
                Quaternionf leftRotation = new Quaternionf(new AxisAngle4f(globalAngle, 0.0f, 1.0f, 0.0f));

                for (Region region : regions.values()) {
                    if (region.getIndicator() == null || !region.getIndicator().isValid()) {
                        if (region.getIndicatorUid() != null && checkDelay >= 20) {
                            Entity entity = Bukkit.getEntity(region.getIndicatorUid());

                            if (entity instanceof BlockDisplay bd) {
                                region.setIndicator(bd);
                                log.info("[Region] 인디케이터가 연결되었습니다. ID: {}", region.Id());
                            } else {
                                var chunk = region.getChunk();
                                if (chunk != null && chunk.isLoaded()) {
                                    Bukkit.getScheduler().runTask(plugin, () -> {
                                        region.setIndicator(rm.indicatorDisplay(region));
                                        rm.saveAsync();
                                        log.warn("[Region] 구역 {}의 인디케이터가 유실되어 자동 재생성되었습니다.", region.Id());
                                    });
                                }
                            }
                        }
                    }
                    setTrans(region, leftRotation);
                }
                if (checkDelay >= 20) checkDelay = 0;
            }
        }.runTaskTimer(plugin, 0L, 3L);
    }

    private void setTrans(@NotNull Region region, @NotNull Quaternionf leftRotation) {
        if (region.getIndicator() != null && region.getIndicator().isValid()) {
            Transformation transformation = region.getIndicator().getTransformation();
            Transformation newTrans = new Transformation(
                    transformation.getTranslation(),
                    leftRotation,
                    transformation.getScale(),
                    transformation.getRightRotation()
            );
            region.getIndicator().setTransformation(newTrans);
        }
    }

    @Override
    public void stopTask() {
        if (this.task == null) return;
        this.task.cancel();
        this.task = null;
    }
}
