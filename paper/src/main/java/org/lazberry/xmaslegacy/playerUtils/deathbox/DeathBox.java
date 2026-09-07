package org.lazberry.xmaslegacy.playerUtils.deathbox;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class DeathBox implements InventoryHolder {
    private final UUID uuid;
    private final Location location;
    private final World world;
    private final Material previousBlock;
    private final Inventory inv;
    private final List<ItemStack> contents = new ArrayList<>();
    private final long spawnedTime;
    private final long expireTime;
    private boolean isExpired = false;

    public DeathBox(@NotNull UUID uuid, @NotNull Location location, @NotNull Material previousBlock,
                    @Nullable List<ItemStack> contents, long remainingTime, String ownerName) {
        this.uuid = uuid;
        this.location = location;
        this.world = location.getWorld();
        this.previousBlock = previousBlock;
        this.spawnedTime = System.currentTimeMillis();
        this.expireTime = remainingTime;

        if (contents != null) {
            for (ItemStack item : contents) {
                if (item != null && item.getType() != Material.AIR)
                    this.contents.add(item.clone());
            }
        }

        int invSize = this.contents.size() > 27 ? 36 : 27;
        this.inv = Bukkit.createInventory(this, invSize, ColorUtils.chat("&c&l" + ownerName + "&r&c&l의 시체상자"));

        for (ItemStack item : this.contents) {
            this.inv.addItem(item);
        }
        world.setType(location, Material.CHEST);
    }

    public DeathBox(@NotNull Player player, @Nullable ItemStack[] contents, long expireTime, boolean hide) {
        if (contents == null || contents.length == 0) setExpired(true);
        this.uuid = player.getUniqueId();
        this.location = player.getLocation().toBlockLocation();
        this.world = location.getWorld();
        this.previousBlock = location.getBlock().getType();
        this.spawnedTime = System.currentTimeMillis();
        this.expireTime = expireTime;

        if (contents != null) {
            for (ItemStack item : contents) {
                if (item != null && item.getType() != Material.AIR)
                    this.contents.add(item.clone());
            }
        }
        String format = hide ? "&c&l" : "&c&k";

        int invSize = this.contents.size() > 27 ? 36 : 27;
        this.inv = Bukkit.createInventory(this, invSize, ColorUtils.chat(format + player.getName() + "&r&c&l의 시체상자"));

        for (ItemStack item : this.contents) {
            this.inv.addItem(item);
        }
        world.setType(location, Material.CHEST);
    }

    public void reset() {
        setExpired(true);
        world.setType(location, previousBlock);
        contents.clear();
        inv.clear();
    }

    public long getRemainingTime() {
        return expireTime - (System.currentTimeMillis() - spawnedTime);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - spawnedTime >= expireTime || isExpired;
    }

    public boolean isEmpty() {
        return inv.isEmpty();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}