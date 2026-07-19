package org.lazberry.xmaslegacy.GhostMode;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;

import java.util.UUID;

@Listeners
public class GhostListener implements Listener {
    private final @NotNull GhostModeManager gmm;
    private final @NotNull XmasLegacy plugin;

	@Inject
    public GhostListener(@NotNull XmasLegacy plugin, @NotNull GhostModeManager gmm) {
		this.plugin = plugin;
		this.gmm = gmm;
    }

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
