package org.lazberry.xmaslegacy.party.tpa;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;

import java.util.UUID;

@Slf4j
@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class TpaListener implements Listener {
	private final XmasLegacy plugin;

	@Inject
	public TpaListener(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player sender = event.getPlayer();
		UUID senderUuid = sender.getUniqueId();

		String locKey = "tpa:target_loc:" + senderUuid;
		String locStr = JedisService.getFromJedis(locKey);

		if (locStr != null) {
			JedisService.removeKey(locKey);

			try {
				String[] parts = locStr.split(",");
				org.bukkit.World world = Bukkit.getWorld(parts[0]);
				double x = Double.parseDouble(parts[1]);
				double y = Double.parseDouble(parts[2]);
				double z = Double.parseDouble(parts[3]);
				float yaw = Float.parseFloat(parts[4]);
				float pitch = Float.parseFloat(parts[5]);

				if (world != null) {
					org.bukkit.Location targetLoc = new org.bukkit.Location(world, x, y, z, yaw, pitch);
					Bukkit.getScheduler().runTaskLater(plugin, () -> sender.teleportAsync(targetLoc), 5L);
				}
			} catch (Exception e) {
				log.error("Failed to parse cached TPA target location for user {}", senderUuid, e);
			}
		}
	}
}
