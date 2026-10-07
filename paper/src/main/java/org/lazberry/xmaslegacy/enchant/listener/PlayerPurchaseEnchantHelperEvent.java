package org.lazberry.xmaslegacy.enchant.listener;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class PlayerPurchaseEnchantHelperEvent extends Event implements Cancellable {
    private static final HandlerList handler = new HandlerList();
    private final Player player;
    private final Inventory inventory;
    private final ItemStack item;
    private int price;
    private boolean cancelled;

    public PlayerPurchaseEnchantHelperEvent(Player player, Inventory inventory, ItemStack item, int price) {
        this.player = player;
        this.inventory = inventory;
        this.item = item;
        this.price = price;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;
    }

    public static HandlerList getHandlerList() {
        return handler;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
