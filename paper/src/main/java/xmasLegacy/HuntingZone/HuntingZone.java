package xmasLegacy.HuntingZone;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmasLegacy.HuntingZone.CustomMobs.MobKey;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class HuntingZone implements HZone {
	private final @NotNull ZoneType type;
	private final @NotNull String worldName;
	private final List<MobKey> mobs;
	private final @NotNull Set<Long> zones;
	private final int maxSpawn = 100;
	private boolean isEnabled = false;

	public HuntingZone(@NotNull ZoneType type, @NotNull String worldName) {
		this.zones = new HashSet<>();
		this.type = type;
		this.worldName = worldName;
		this.mobs = Arrays.stream(type.getMobs()).toList();
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

	public boolean getEnabled() {
		return this.isEnabled;
	}
}
