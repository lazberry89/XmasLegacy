package org.lazberry.xmaslegacy.airdrop.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class AirdropLandEvent extends Event {
    private static final HandlerList handler = new HandlerList();
    private final Location location;
    private final Chest supplyChest;
    private ItemStack[] contents;

    public AirdropLandEvent(Location location, Chest supplyChest, ItemStack[] contents) {
        this.location = location;
        this.supplyChest = supplyChest;
        this.contents = contents;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;
    }

    public static HandlerList getHandlerList() {
        return handler;
    }
}
