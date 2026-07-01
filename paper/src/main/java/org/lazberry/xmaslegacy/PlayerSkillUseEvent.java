package org.lazberry.xmaslegacy;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Emblems.Emblem;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;

public class PlayerSkillUseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    @Getter
    private final @NotNull Player player;
    private final @NotNull UsingEnergy usingEnergy;
    private final @NotNull Emblem emblem;
    private final @NotNull EmblemType type;
    private boolean cancelled = false;

    public PlayerSkillUseEvent(@NotNull Player player, @NotNull UsingEnergy usingEnergy, @NotNull Emblem emblem, @NotNull EmblemType type) {
        this.player = player;
        this.usingEnergy = usingEnergy;
        this.emblem = emblem;
        this.type = type;
    }

    public UsingEnergy getRoleInstance() {
        return this.usingEnergy;
    }
    public @NotNull Emblem getEmblem() {
        return this.emblem;
    }
    public @NotNull EmblemType getType() {
        return this.type;
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
        return handlers;
    }
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
