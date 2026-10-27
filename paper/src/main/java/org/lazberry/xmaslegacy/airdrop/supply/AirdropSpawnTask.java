package org.lazberry.xmaslegacy.airdrop.supply;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.plugin.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Task
@Slf4j
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class AirdropSpawnTask implements Tasks {
    private final AirdropSpawnManager spawnManager;
    private BukkitTask task;

    @Inject
    public AirdropSpawnTask(AirdropSpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }

    @Override
    public void startTask(@NotNull XmasLegacy plugin) {
		if (spawnManager.getCenter() == null) {
			log.error("Spawn Manager Center is null");
			return;
		}

        if (task == null) task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            spawnManager.dropAirdropRandomly();
            log.info("Successfully dropped airdrop.");
        }, 30L, spawnManager.getSpawnIntervalTicks());
    }

    @Override
    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
