package org.lazberry.xmaslegacy.PartyScoreBoard;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lazberry.xmaslegacy.Annotation.Listeners;
import org.lazberry.xmaslegacy.Utils.BoardUtils;

@Slf4j
@Listeners
public class BoardListener implements Listener {

	@EventHandler
	public void removeBoardWhenLeave(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		BoardUtils.removeBoard(p);
		log.info("Successfully removed {}'s board", p.getName());
	}
}
