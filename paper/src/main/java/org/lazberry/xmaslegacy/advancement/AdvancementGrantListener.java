package org.lazberry.xmaslegacy.advancement;

import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class AdvancementGrantListener implements Listener {
	private final XmasLegacyAdvancements advancements;

	@Inject
	public AdvancementGrantListener(XmasLegacyAdvancements advancements) {
		this.advancements = advancements;
	}

	@EventHandler
	public void grantAdvancementWhenIsFirstJoin(PlayerJoinEvent event) {
		var player = event.getPlayer();
		if (!player.hasPlayedBefore()) {
			var key = new AdvancementKey(advancements.getPlugin(), "root_waypoint");
			var advancement = advancements.getApi().getAdvancement(key);

			if (advancement != null) advancements.grantGlobalAdvancement(player, advancement);
		}
	}
}
