package org.lazberry.xmaslegacy.rank;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;

@Listeners
@Components.Exclude(type = ServerType.LOBBY)
public class RankListener implements Listener {
	private final UserManager userManager;
	private final RankManager rankManager;

	@Inject
	public RankListener(UserManager userManager, RankManager rankManager) {
		this.userManager = userManager;
		this.rankManager = rankManager;
	}

	/**
	 * Before ServerJoinListener (HIGHEST) unloads the user. Joins need nothing: RankTask updates online players.
	 * Never load a user here, it would put the leaving player back into the cache with old data.
	 */
	@EventHandler
	public void updateUserOnLeave(PlayerQuitEvent event) {
		User user = userManager.getUser(event.getPlayer().getUniqueId());
		if (user != null) rankManager.updateUser(user);
	}
}
