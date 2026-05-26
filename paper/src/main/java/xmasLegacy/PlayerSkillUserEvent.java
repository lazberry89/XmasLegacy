package xmasLegacy;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import xmasLegacy.Emblems.Emblem;
import xmasLegacy.Emblems.EmblemType;
import xmasLegacy.SecondaryRoleManager.AbstractSecondRole;

public class PlayerSkillUserEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final AbstractSecondRole roleClass;
    private final Emblem emblem;
    private final EmblemType type;
    private boolean cancelled = false;

    public PlayerSkillUserEvent(Player player, AbstractSecondRole roleClass, Emblem emblem, EmblemType type) {
        this.player = player;
        this.roleClass = roleClass;
        this.emblem = emblem;
        this.type = type;
    }

    public Player getPlayer() {
        return this.player;
    }
    public AbstractSecondRole getRoleClass() {
        return this.roleClass;
    }
    public Emblem getEmblem() {
        return this.emblem;
    }
    public EmblemType getType() {
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
