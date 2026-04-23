package org.lazberry.xmasLegacy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@SuppressWarnings("ClassCanBeRecord")
public class GhostListener implements Listener {
    private final GhostModeManager GMM;
    private final XmasLegacy plugin;

    public GhostListener(GhostModeManager GMM, XmasLegacy plugin) {
        this.GMM = GMM;
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!p.isOp()) return;
        if (GMM.isGhostMode(p)) {
            GMM.toggle(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player joined = e.getPlayer();
        for (UUID uuid : GMM.isGhostMode().keySet()) {
            if (GMM.isGhostMode().get(uuid)) {
                Player admin = Bukkit.getPlayer(uuid);
                if (admin != null) joined.hidePlayer(plugin, admin);
            }
        }
    }
}
