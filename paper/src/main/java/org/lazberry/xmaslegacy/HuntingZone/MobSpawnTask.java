package org.lazberry.xmaslegacy.HuntingZone;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.HuntingZone.CustomMobs.MobRepository;
import org.lazberry.xmaslegacy.HuntingZone.CustomMobs.CustomMob;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Task
@Registry.Include(type = ServerType.HUNTING)
public class MobSpawnTask implements Tasks {
	private final @NotNull HuntingZoneManager hzm;
	private final @NotNull MobRepository mr;
	private @Nullable BukkitTask task;

	@Inject
	public MobSpawnTask(@NotNull HuntingZoneManager hzm, @NotNull MobRepository mr) {
		this.hzm = hzm;
		this.mr = mr;
	}

	public @NotNull Location getRandomLocationInChunk(@NotNull Chunk chunk) {
		World world = chunk.getWorld();
		ThreadLocalRandom random = ThreadLocalRandom.current();

		int startX = chunk.getX() << 4;
		int startZ = chunk.getZ() << 4;

		int finalX = startX + random.nextInt(16);
		int finalZ = startZ + random.nextInt(16);
		int finalY = world.getHighestBlockYAt(finalX, finalZ);

		return new Location(world, finalX + 0.5, finalY, finalZ + 0.5);
	}

	private @NotNull List<CustomMob> getRandomMobs(@NotNull ZoneType type, int count) {
		CustomMob[] availableMobs = this.mr.getMobInstance(type);
		if (availableMobs == null || availableMobs.length == 0) return Collections.emptyList();

		List<CustomMob> result = new ArrayList<>();
		ThreadLocalRandom random = ThreadLocalRandom.current();

		for (int i = 0; i < count; i++) {
			int randomIndex = random.nextInt(availableMobs.length);
			result.add(availableMobs[randomIndex]);
		}
		return result;
	}

	public void spawn(@NotNull ZoneType type) {
		HuntingZone zone = this.hzm.getZone(type);
		if (!zone.isEnabled()) return;

		int currentMobCount = zone.getAliveMobCount();
		int maxSpawnLimit = zone.getMaxSpawn();

		if (currentMobCount >= maxSpawnLimit) return;

		Chunk[] chunks = zone.zones();
		ThreadLocalRandom random = ThreadLocalRandom.current();

		for (Chunk c : chunks) {
			if (!c.isLoaded()) continue;

			int spawnChance = random.nextInt(3);
			if (spawnChance > 0) {
				if (currentMobCount + spawnChance > maxSpawnLimit) {
					int remainingSlots = maxSpawnLimit - currentMobCount;
					if (remainingSlots > 0) {
						getRandomMobs(type, remainingSlots).forEach(mob -> mob.spawn(getRandomLocationInChunk(c)));
					}
					break;
				}
				getRandomMobs(type, spawnChance).forEach(mob -> mob.spawn(getRandomLocationInChunk(c)));
				currentMobCount += spawnChance;
			}
		}
	}

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		if (this.task != null) return;
		this.task = Bukkit.getScheduler().runTaskTimer(plugin, () ->
			hzm.getZones().stream()
					.filter(HuntingZone::isEnabled)
					.forEach(z -> spawn(z.getType()))
		, 0L, 20 * 60L);
	}

	@Override
	public void stopTask() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
	}
}