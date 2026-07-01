package org.lazberry.xmaslegacy.HuntingZone;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.HuntingZone.CustomMobs.MobKey;
import org.lazberry.xmaslegacy.HuntingZone.CustomMobs.MobRepository;
import org.lazberry.xmaslegacy.HuntingZone.CustomMobs.Unrated.CustomMob;

import java.util.*;

public class HuntingZone implements HZone {
	private final @NotNull ZoneType type;
	private final @NotNull String worldName;
	private final @NotNull MobRepository mobRepository;
	private final @NotNull List<MobKey> mobs;
	private final @NotNull Set<Long> zones;
	private final @NotNull Set<MobKey> mobKeySet;
    private boolean isEnabled = false;

	public HuntingZone(@NotNull ZoneType type, @NotNull String worldName) {
		this.zones = new HashSet<>();
		this.type = type;
		this.worldName = worldName;
		this.mobRepository = MobRepository.INSTANCE;
		this.mobs = Arrays.stream(type.getMobs()).toList();
		this.mobKeySet = new HashSet<>(this.mobs);
	}

	public void enLarge(@NotNull Player p) {
		this.zones.add(p.getLocation().getChunk().getChunkKey());
	}
	public void enLarge(@NotNull Chunk c) {
		this.zones.add(c.getChunkKey());
	}
	public void enLarge(@NotNull Location loc) {
		this.zones.add(loc.getChunk().getChunkKey());
	}

	public void shrink(@NotNull Chunk c) {
		this.zones.remove(c.getChunkKey());
	}
	public void shrink(@NotNull Location loc) {
		this.zones.remove(loc.getChunk().getChunkKey());
	}
	public void shrink(@NotNull Player p) {
		this.zones.remove(p.getLocation().getChunk().getChunkKey());
	}

	public boolean inZone(@NotNull Player p) {
		return this.zones.contains(p.getLocation().getChunk().getChunkKey());
	}
	public boolean inZone(@NotNull Chunk c) {
		return this.zones.contains(c.getChunkKey());
	}
	public boolean inZone(@NotNull Location loc) {
		return this.zones.contains(loc.getChunk().getChunkKey());
	}

	public @NotNull Chunk[] zones() {
		World world = Bukkit.getWorld(this.worldName);
		if (world == null) return new Chunk[0];

		return this.zones.stream()
				.map(key -> {
					long k = key;

					int x = (int) k;
					int z = (int) (k >> 32);

					return world.getChunkAt(x, z);
				})
				.toArray(Chunk[]::new);
	}

	public MobKey[] getMobs() {
		return this.mobs.toArray(MobKey[]::new);
	}
	public @NotNull ZoneType getType() {
		return this.type;
	}
	public void enable() {
		this.isEnabled = true;
	}
	public void disable() {
		this.isEnabled = false;
	}
	public boolean isEnabled() {
		return this.isEnabled;
	}
	public int getMaxSpawn() {
        return 100;
	}

	public int getAliveMobCount() {
		World world = Bukkit.getWorld(this.worldName);
		if (world == null) return 0;

		int totalCount = 0;

		for (long k : this.zones) {
			int x = (int) k;
			int z = (int) (k >> 32);

			if (world.isChunkLoaded(x, z)) {
				Chunk chunk = world.getChunkAt(x, z);

				for (Entity entity : chunk.getEntities()) {
					if (entity instanceof LivingEntity le && isCustomMobOfThisZone(le)) totalCount++;
				}
			}
		}
		return totalCount;
	}

	private boolean isCustomMobOfThisZone(@Nullable LivingEntity entity) {
		for (MobKey mobKey : this.mobKeySet) {
			CustomMob mob = mobRepository.getMobInstance(mobKey, CustomMob.class);
			if (mob != null && mob.isEntity(entity)) return true;
		}
		return false;
	}
}
