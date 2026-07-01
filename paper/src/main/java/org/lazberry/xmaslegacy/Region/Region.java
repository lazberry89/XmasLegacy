package org.lazberry.xmaslegacy.Region;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.IDGenerator;

import java.util.*;

@SuppressWarnings("unused")
public class Region {
    private final @NotNull UUID owner;
    private final @NotNull String id;
    private final @NotNull World world;
    private @Nullable BlockDisplay indicator;
    private @Nullable UUID indicatorId;
    private final long key;

    private boolean allowPublicEntry = true;
    private boolean allowPublicInteraction = false;

    public Region(Player p, Location loc) {
        this.owner = p.getUniqueId();
        this.id = IDGenerator.generateRandomId();
        this.world = loc.getWorld();
        var chunk = loc.getChunk();
        this.key = chunk.isLoaded() && chunk.isGenerated() ? chunk.getChunkKey() : -1;
    }

    public Region(@NonNull UUID uuid, @NonNull String id, @NonNull World world, long key, boolean allowPublicEntry, boolean allowPublicInteraction) {
        this.owner = uuid;
        this.id = id;
        this.world = world;
        this.key = key;
		this.allowPublicEntry = allowPublicEntry;
		this.allowPublicInteraction = allowPublicInteraction;
    }

    public void setIndicator(BlockDisplay display) {
        this.indicator = display;
        this.indicatorId = display != null ? display.getUniqueId() : null;
    }

    public void setIndicator(UUID uuid) {
        this.indicatorId = uuid;
    }

    public @Nullable BlockDisplay getIndicator() {
        return this.indicator;
    }

    public @Nullable UUID getIndicatorUid() {
        return this.indicatorId;
    }

    public boolean isValid() {
        return key != -1;
    }

    public @NotNull World getWorld() {
        return this.world;
    }

    public boolean isInside(@NotNull Location loc) {
        if (!loc.getWorld().equals(this.world)) return false; // 월드가 다르면 바로 컷
        return loc.getChunk().getChunkKey() == this.key;
    }
    public boolean isInside(Chunk c) {
        if (!c.getWorld().equals(this.world)) return false;
        return c.getChunkKey() == this.key;
    }

    public @Nullable Chunk getChunk() {
        int x = (int) key;
        int z = (int) (key >> 32);

        return this.world.getChunkAt(x, z);
    }

	public int getChunkX() {
		return (int) (this.key >> 32);
	}
	public int getChunkZ() {
		return (int) this.key;
	}
    public @NotNull UUID getOwner() {
        return this.owner;
    }
    public @NotNull String Id() {
        return this.id;
    }
    public long key() {
        return this.key;
    }

    public boolean isInteractionAllowed() {
        return this.allowPublicInteraction;
    }
    public void allowInteraction() {
        this.allowPublicInteraction = true;
    }
    public void blockInteraction() {
        this.allowPublicInteraction = false;
    }
    public boolean isEntryAllowed() {
        return this.allowPublicEntry;
    }
    public void allowEntry() {
        this.allowPublicEntry = true;
    }
    public void blockEntry() {
        this.allowPublicEntry = false;
    }
    public Location getTrueCenter(@NotNull Chunk chunk) {
        int minX = chunk.getX() << 4;
        int minZ = chunk.getZ() << 4;

        double centerX = minX + 7.5;
        double centerZ = minZ + 7.5;
        double centerY = chunk.getWorld().getHighestBlockYAt(minX + 8, minZ + 8) + 1.0;

        return new Location(chunk.getWorld(), centerX, centerY, centerZ);
    }
    public Location getBlockCenter(@NotNull Chunk chunk) {
        int minX = chunk.getX() << 4;
        int minZ = chunk.getZ() << 4;

        int centerX = minX + 8;
        int centerZ = minZ + 8;
        int centerY = chunk.getWorld().getHighestBlockYAt(centerX, centerZ);

        return new Location(chunk.getWorld(), centerX, centerY, centerZ);
    }

    public void toggleEntry() {
        if (isEntryAllowed()) blockEntry();
        else allowEntry();
    }

    public void toggleInteraction() {
        if (isInteractionAllowed()) blockInteraction();
        else allowInteraction();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Region r)) return false;
        return r.Id().equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

}
