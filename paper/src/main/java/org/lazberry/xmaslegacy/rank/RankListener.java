package org.lazberry.xmaslegacy.rank;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.UserHandler;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class RankListener implements Listener {
	private final UserManager userManager;
	private final RankManager rankManager;

	@Inject
	public RankListener(UserManager userManager, RankManager rankManager) {
		this.userManager = userManager;
		this.rankManager = rankManager;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void updateUserOnJoin(PlayerJoinEvent event) {
		updateProcess(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void updateUserOnLeave(PlayerQuitEvent event) {
		updateProcess(event.getPlayer());
	}

	private void updateProcess(Player player) {
		var uuid = player.getUniqueId();

		User user = userManager.getUser(uuid);
		if (user == null) {
			UserHandler.loadUser(player, true, rankManager::updateUser);
			return;
		}
		rankManager.updateUser(user);
	}
}

