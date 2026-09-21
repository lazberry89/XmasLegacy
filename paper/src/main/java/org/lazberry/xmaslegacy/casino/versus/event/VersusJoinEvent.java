package org.lazberry.xmaslegacy.casino.versus.event;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.casino.versus.VersusField;

@Getter
public class VersusJoinEvent extends Event implements Cancellable {
    private static final HandlerList handler = new HandlerList();
    private final Player player;
    private final VersusField field;
    private boolean cancel = false;

    public VersusJoinEvent(Player player, VersusField field) {
        this.player = player;
        this.field = field;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

    public static HandlerList getHandlerList() {
        return handler;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;
    }
}
