package org.lazberry.xmaslegacy.icing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.utils.UserHandler;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.concurrent.ThreadLocalRandom;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class IcingListener implements Listener {
	private final @NotNull UserManager um;
	private final @NotNull IcingBossBarManager bm;

	@Inject
	public IcingListener(@NotNull UserManager um, @NotNull IcingBossBarManager bm) {
		this.um = um;
		this.bm = bm;
	}

	@EventHandler
	public void resetIcingWhenDead(PlayerDeathEvent e) {
		Player p = e.getPlayer();
		var user = um.getUser(p.getUniqueId());
		if (user != null) user.setIcingState(100);
	}

	@EventHandler
	public void removeWhenLeave(PlayerQuitEvent e) {
		bm.removeBar(e.getPlayer());
	}

	@EventHandler
	public void removeImmuneWhenJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		var user = um.getUser(p.getUniqueId());
		if (user == null) return;

		if (user.isImmuneToIcing()) user.setImmuneToIcing(false);
	}

	@EventHandler
	public void rechargeIcingStateWhenConsumeSunFlower(PlayerItemConsumeEvent e) {
		final var key = KeyUtils.get("farmer");
		Player p = e.getPlayer();
		ItemStack item = e.getItem();

		var user = um.getUser(p.getUniqueId());
		if (user == null) {
			UserHandler.sendReloadNotice(p);
			return;
		}

		if (KeyUtils.hasKey(item, key, PersistentDataType.STRING, "sunflower_bread")) {
			user.addIcingState(ThreadLocalRandom.current().nextInt(10, 16));
			p.setFoodLevel(p.getFoodLevel() + 4);
			p.setSaturation(p.getSaturation() + 4);
		}
	}
}
