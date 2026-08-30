package org.lazberry.xmaslegacy.utils;

import org.bukkit.Bukkit;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;

public final class Vignette {

    public static void sendVignetteEffect(Player player) {
        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(player.getLocation());
        border.setSize(10000);
        border.setWarningDistance(10000);
        player.setWorldBorder(border);
    }

    public static void clearVignette(Player player) {
        player.setWorldBorder(null);
    }
}
