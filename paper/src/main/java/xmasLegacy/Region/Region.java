package xmasLegacy.Region;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.IDGenerator;

import java.util.*;

@SuppressWarnings("unused")
public class Region {
    private final @NotNull UUID owner;
    private final @NotNull String id;
    private final @NotNull World world;
    private final long key;

    private boolean allowPublicEntry = true;
    private boolean allowPublicInteraction = false;

    public Region(Player p, Location loc) {
        this.owner = p.getUniqueId();
        this.id = IDGenerator.generateRandomId(Constants.ID_LENGTH);
        this.world = loc.getWorld();
        var chunk = loc.getChunk();
        this.key = chunk.isLoaded() && chunk.isGenerated() ? chunk.getChunkKey() : -1;
    }

    public Region(@NonNull UUID uuid, @NonNull String id, @NonNull World world, long key) {
        this.owner = uuid;
        this.id = id;
        this.world = world;
        this.key = key;
    }

    @CheckReturnValue
    public boolean isValid() {
        return key != -1;
    }

    public World getWorld() {
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
