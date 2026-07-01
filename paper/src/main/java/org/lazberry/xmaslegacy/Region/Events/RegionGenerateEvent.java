package org.lazberry.xmaslegacy.Region.Events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.Region.Region;

import java.util.UUID;

public class RegionGenerateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
	private final @NotNull Player player;
    private final @NotNull UUID owner;
    private final @NotNull Region region;
    private final @NotNull String id;
    private boolean cancel = false;

    public RegionGenerateEvent(@NotNull Player p, @NonNull Region region, @NonNull UUID owner, @NonNull String id) {
		this.player = p;
        this.region = region;
        this.owner = owner;
        this.id = id;
    }
	public @NotNull Player getPlayer() {
		return this.player;
	}

    public @NotNull UUID getOwner() {
        return this.owner;
    }

    public @NotNull Region getRegion() {
        return this.region;
    }

    public @NotNull String Id() {
        return this.id;
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }
    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
