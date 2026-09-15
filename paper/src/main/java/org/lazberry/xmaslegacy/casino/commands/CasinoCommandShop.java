package org.lazberry.xmaslegacy.casino.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.shop.CasinoShop;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public class CasinoCommandShop implements SubCommand {
    private final CasinoShop shop;

    public CasinoCommandShop(XmasLegacy plugin) {
        this.shop = new CasinoShop(plugin);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 1 && player.isOp()) {
            player.openInventory(shop.getInventory());
        } else InfoUtils.error(player, "Cannot execute the command.");
    }
}
