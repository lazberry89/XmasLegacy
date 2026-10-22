package org.lazberry.xmaslegacy.rank.playtime;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class PlaytimeUpdateListener implements Listener {
    private final UserPlaytimeManager playtimeManager;

    @Inject
    public PlaytimeUpdateListener(UserPlaytimeManager playtimeManager) {
        this.playtimeManager = playtimeManager;
    }

    private void update(Player player) {
        var uuid = player.getUniqueId();
        playtimeManager.updateUserPlaytime(uuid);
    }

    @EventHandler
    public void updateWhenJoin(PlayerJoinEvent e) {
        update(e.getPlayer());
    }

    @EventHandler
    public void updateWhenQuit(PlayerQuitEvent e) {
        update(e.getPlayer());
    }
}
