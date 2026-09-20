package org.lazberry.xmaslegacy.utils;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class Spectate {

    public static void setSpectator(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setInvisible(true);
    }
}
