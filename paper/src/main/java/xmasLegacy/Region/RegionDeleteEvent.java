package xmasLegacy.Region;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RegionDeleteEvent extends Event implements Cancellable {
    private static final HandlerList handler = new HandlerList();
    private final @NotNull UUID owner;
    private final @NotNull Region region;
    private final @NotNull String id;
    private boolean cancelled = false;

    public @NotNull UUID getOwner() {
        return owner;
    }
    public @NotNull Region getRegion() {
        return region;
    }
    public @NotNull String Id() {
        return id;
    }

    public RegionDeleteEvent(@NotNull UUID owner, @NotNull Region region, @NotNull String id) {
        this.owner = owner;
        this.region = region;
        this.id = id;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;
    }
    public static HandlerList getHandlerList() {
        return handler;
    }
}
