package org.lazberry.xmaslegacy.icing;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.user.User;

@Getter
public class PlayerIcingStateReduceEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final User user;
    private final int previousState;
    private @Setter int nextAmount;
    private boolean isCancelled = false;

    public PlayerIcingStateReduceEvent(Player player, User user, int previousState, int nextAmount) {
        this.player = player;
        this.user = user;
        this.previousState = previousState;
        this.nextAmount = nextAmount;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        isCancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
