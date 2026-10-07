package org.lazberry.xmaslegacy.enchant.listener;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class PlayerEnchantEvent extends Event implements Cancellable {
    private static final HandlerList handler = new HandlerList();
    private final Player player;
    private final ItemStack item;
    private final int currentLevel;
    private final int neededAmount;
    private boolean cancelled = false;
    private boolean preventBreak = false;
    private boolean preventDowngrade = false;
    private double bonusChance = 0.0;

    public PlayerEnchantEvent(Player player, ItemStack item, int currentLevel, int neededAmount) {
        this.player = player;
        this.item = item;
        this.currentLevel = currentLevel;
        this.neededAmount = neededAmount;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;
    }

    public static HandlerList getHandlerList() {
        return handler;
    }
}
