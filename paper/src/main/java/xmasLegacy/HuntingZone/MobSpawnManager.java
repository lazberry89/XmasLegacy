package xmasLegacy.HuntingZone;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmasLegacy.HuntingZone.CustomMobs.MobRepository;
import xmasLegacy.HuntingZone.CustomMobs.Unrated.CustomMob;
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MobSpawnManager {
	private final @NotNull XmasLegacy plugin;
	private final @NotNull HuntingZoneManager hzm;
	private final @NotNull MobRepository mr;
	private @Nullable BukkitTask task;
	private static @Nullable MobSpawnManager instance;

	public static MobSpawnManager getInstance() {
		if (instance == null) instance = new MobSpawnManager();
		return instance;
	}

	@ApiStatus.Internal
	private MobSpawnManager() {
		this.plugin = XmasLegacy.getInstance();
		this.hzm = HuntingZoneManager.getInstance();
		this.mr = MobRepository.getInstance();
	}

	public @NotNull Location getRandomLocationInChunk(Chunk chunk) {
		World world = chunk.getWorld();
		ThreadLocalRandom random = ThreadLocalRandom.current();

		int startX = chunk.getX() << 4;
		int startZ = chunk.getZ() << 4;

		int finalX = startX + random.nextInt(16);
		int finalZ = startZ + random.nextInt(16);
		int finalY = world.getHighestBlockYAt(finalX, finalZ);

		return new Location(world, finalX + 0.5, finalY, finalZ + 0.5);
	}

	private @NotNull List<CustomMob> getRandomMobs(ZoneType type, int count) {
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

	public void spawn(ZoneType type) {
		HuntingZone zone = this.hzm.getZones(type);
		if (zone == null || !zone.getEnabled()) return;

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

	public void startTask() {
		if (this.task != null) return;
		this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, () ->
			hzm.getZones().stream()
					.filter(HuntingZone::getEnabled)
					.forEach(z -> spawn(z.getType()))
		, 0L, 20 * 60L);
	}

	public void stopTask() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
	}
}