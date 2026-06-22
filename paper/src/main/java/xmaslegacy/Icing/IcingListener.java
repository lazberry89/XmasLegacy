package xmaslegacy.Icing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class IcingListener implements Listener {

	@EventHandler
	public void resetIcingWhenDead(PlayerDeathEvent e) {
		Player p = e.getPlayer();
		IcingSystem.INSTANCE.setState(p.getUniqueId(), 100);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		IcingSystem.INSTANCE.removeBar(p);
	}
}
