package xmaslegacy.Icing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Utils.KeyUtils;
import xmaslegacy.Utils.ServerTransfer;

import java.util.concurrent.ThreadLocalRandom;

public class IcingListener implements Listener {
	private final @NotNull UserManager um;

	public IcingListener() {
		this.um = UserManager.INSTANCE;
	}

	@EventHandler
	public void resetIcingWhenDead(PlayerDeathEvent e) {
		Player p = e.getPlayer();
		var user = um.getUser(p.getUniqueId());
		if (user != null) user.setIcingState(100);
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
			ServerTransfer.sendReloadNotice(p);
			return;
		}

		if (KeyUtils.hasKey(item, key, PersistentDataType.STRING, "sunflower_bread"))
			user.addIcingState(ThreadLocalRandom.current().nextInt(10, 16));
	}
}
