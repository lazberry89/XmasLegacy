package xmasLegacy;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import xmasLegacy.Emblems.Emblem;
import xmasLegacy.Emblems.EmblemType;
import xmasLegacy.SecondaryRoleManager.AbstractSecondRole;

public class PlayerSkillUseEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final UsingEnergy usingEnergy;
    private final Emblem emblem;
    private final EmblemType type;
    private boolean cancelled = false;

    public PlayerSkillUseEvent(Player player, UsingEnergy usingEnergy, Emblem emblem, EmblemType type) {
        this.player = player;
        this.usingEnergy = usingEnergy;
        this.emblem = emblem;
        this.type = type;
    }

    public Player getPlayer() {
        return this.player;
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
