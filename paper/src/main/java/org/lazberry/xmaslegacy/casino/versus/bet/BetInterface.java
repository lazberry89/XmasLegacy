package org.lazberry.xmaslegacy.casino.versus.bet;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

public class BetInterface implements InventoryHolder {
    private final Inventory inv;

    public BetInterface(Player fighter1, Player fighter2, XmasLegacy plugin) {
        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&c&l인게임 베팅"));
        var bg = bg(plugin);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, bg);
        }
        this.inv.setItem(3, headCreator(plugin, fighter1));
        this.inv.setItem(4, null);
        this.inv.setItem(5, headCreator(plugin, fighter2));
    }

    private ItemStack bg(XmasLegacy plugin) {
        return ItemBuilder.of(plugin, Material.GRAY_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat(""))
                .setLore(ColorUtils.chat(""))
                .hideAllFlags()
                .build();
    }

    private ItemStack headCreator(XmasLegacy plugin, Player player) {
        return ItemBuilder.of(plugin, Material.PLAYER_HEAD)
                .setName(player.name())
                .setHeadOwner(player)
                .setLore(ColorUtils.chat("&7클릭하여 베팅 설정하기"))
                .build();
    }

    public int getBetAmount() {
        ItemStack item = this.inv.getItem(4);
        if (Casino.isCoin(item)) return item.getAmount();
        return 0;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inv;
    }
}
