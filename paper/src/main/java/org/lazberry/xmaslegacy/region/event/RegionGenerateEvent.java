package org.lazberry.xmaslegacy.region.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.region.Region;

import java.util.UUID;

@Getter
public class RegionGenerateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
	private final Player player;
    private final UUID owner;
    private final Region region;
    private final String id;
    private boolean cancel = false;

    public RegionGenerateEvent(Player p, Region region, UUID owner, String id) {
		this.player = p;
        this.region = region;
        this.owner = owner;
        this.id = id;
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
