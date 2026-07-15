package org.lazberry.xmaslegacy.GhostMode;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Listeners;
import org.lazberry.xmaslegacy.Annotation.Plugin;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Manager;

import java.util.UUID;

@Inject
@Listeners
public class GhostListener implements Listener {
    private @Manager @NotNull GhostModeManager gmm;
    private @Plugin @NotNull XmasLegacy plugin;

    public GhostListener() {}

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (!p.isOp()) return;
        if (gmm.isGhostMode(p)) {
            gmm.toggle(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player joined = e.getPlayer();
        for (UUID uuid : gmm.getGhostModePlayer()) {
            if (gmm.isGhostMode(uuid)) {
                Player admin = Bukkit.getPlayer(uuid);
                if (admin != null) joined.hidePlayer(plugin, admin);
            }
        }
    }
}
