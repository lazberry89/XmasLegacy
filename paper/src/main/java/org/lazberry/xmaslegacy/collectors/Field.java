package org.lazberry.xmaslegacy.collectors;

import lombok.Data;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Data
public class Field {
	private final Difficulty difficulty;
	private final Location pos1;
	private final Location pos2;
	private final Location spawn;
	private final World world;
	private final List<Location> potentialDropLocations = new ArrayList<>();
	private final Map<Location, Integer> spawnedDropContainerLocations = new HashMap<>();
	private boolean isRunning = false;

	public Field(Difficulty difficulty, Location pos1, Location pos2, Location spawn) {
		this.difficulty = difficulty;
		this.pos1 = pos1;
		this.pos2 = pos2;
		this.spawn = spawn;
		this.world = spawn.getWorld();
	}

	public boolean addDropLocation(Location location) {
		return potentialDropLocations.add(location.toBlockLocation());
	}

	public boolean removeDropLocation(Location location) {
		return potentialDropLocations.remove(location.toBlockLocation());
	}

	public void spawnDropContainers(int count) {
		if (count <= 0) return;

		List<Location> dropLocs = snapshotOfDropLocations();
		if (dropLocs.size() < count) return;

		cleanDropContainers();

		Collections.shuffle(dropLocs);
		for (int i = 0; i < count; i++) {
			var loc = dropLocs.get(i);
			loc.getBlock().setType(Material.DECORATED_POT, true);
			world.spawnParticle(Particle.END_ROD, loc, 7, 0.2, 0.2, 0.2, 0.01);
			world.playSound(loc, Sound.BLOCK_DECORATED_POT_PLACE, 1.0f, 1.0f);

			spawnedDropContainerLocations.put(loc.toBlockLocation(), ThreadLocalRandom.current()
					.nextInt(difficulty.getMinimumHit(), difficulty.getMaximumHit() + 1));
		}
	}

	public int getPotHealth(Block block) {
		return spawnedDropContainerLocations.getOrDefault(block.getLocation(), 0);
	}

	public void cleanDropContainers() {
		spawnedDropContainerLocations.keySet()
				.forEach(l -> l.getBlock().setType(Material.AIR));
		spawnedDropContainerLocations.clear();
	}

	public boolean isDropContainer(Location location) {
		return spawnedDropContainerLocations.containsKey(location.toBlockLocation());
	}

	public boolean isDropContainer(Block block) {
		return isDropContainer(block.getLocation());
	}

	public List<Location> snapshotOfDropLocations() {
		return Collections.unmodifiableList(potentialDropLocations);
	}
}
