package org.lazberry.xmaslegacy.region;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.utils.IDGenerator;

import java.util.UUID;

@Getter @Setter
public class Region {
    public static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(World.class, (JsonSerializer<World>) (world, type, context) ->
                    new JsonPrimitive(world.getUID().toString()))
            .registerTypeAdapter(World.class, (JsonDeserializer<World>) (json, type, context) ->
                Bukkit.getWorld(UUID.fromString(json.getAsString()))).create();

    public String toGson() {
        return GSON.toJson(this);
    }

    public static Region fromGson(String gson) {
        if (gson == null || gson.isEmpty()) return null;
        return GSON.fromJson(gson, Region.class);
    }

    private final UUID owner;
    private final String id;
    private final World world;
    private final long key;

    private transient @Nullable BlockDisplay indicator;
    private transient @Nullable Interaction interaction;
    private boolean allowPublicEntry = true;
    private boolean allowPublicInteraction = false;

    public Region(Player p, Location loc) {
        this.owner = p.getUniqueId();
        this.id = IDGenerator.generateRandomId(Constants.ID_LENGTH);
        this.world = loc.getWorld();
        var chunk = loc.getChunk();
        this.key = chunk.isLoaded() && chunk.isGenerated() ? chunk.getChunkKey() : -1;
    }

    public Region(UUID uuid, String id, World world, long key, boolean allowPublicEntry, boolean allowPublicInteraction) {
        this.owner = uuid;
        this.id = id;
        this.world = world;
        this.key = key;
		this.allowPublicEntry = allowPublicEntry;
		this.allowPublicInteraction = allowPublicInteraction;
    }

    public boolean isValid() {
        return key != -1;
    }

    public boolean isInside(@NotNull Location loc) {
        if (!loc.getWorld().equals(this.world)) return false; // 월드가 다르면 바로 컷
        return loc.getChunk().getChunkKey() == this.key;
    }

    public boolean isInside(@NotNull Chunk c) {
        if (!c.getWorld().equals(this.world)) return false;
        return c.getChunkKey() == this.key;
    }

    public Chunk getChunk() {
        return world.getChunkAt(getChunkX(), getChunkZ());
    }

    public int getChunkX() {
        return (int) this.key;
    }
    public int getChunkZ() {
        return (int) (this.key >> 32);
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

	@Contract("_ -> new")
    public @NotNull Location getTrueCenter(@NotNull Chunk chunk) {
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
        return r.getId().equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

}
