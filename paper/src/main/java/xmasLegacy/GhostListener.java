package xmasLegacy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Listeners
public class GhostListener implements Listener {
    private final @NotNull GhostModeManager gmm;
    private final @NotNull XmasLegacy plugin;

    public GhostListener() {
        this.gmm = GhostModeManager.INSTANCE;
        this.plugin = XmasLegacy.getInstance();
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
        for (UUID uuid : gmm.isGhostMode()) {
            if (gmm.isGhostMode(uuid)) {
                Player admin = Bukkit.getPlayer(uuid);
                if (admin != null) joined.hidePlayer(plugin, admin);
            }
        }
    }
}
