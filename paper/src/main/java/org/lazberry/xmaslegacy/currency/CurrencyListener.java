package org.lazberry.xmaslegacy.currency;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.EconomyManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.UUID;

@Registry.Exclude(type = ServerType.LOBBY)
public class CurrencyListener implements Listener {
	private final EconomyManager economyManager;

	@Inject
	public CurrencyListener(EconomyManager economyManager) {
		this.economyManager = economyManager;
	}

	@EventHandler
	public void rightClickWithCurrency(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		if (event.getAction().isRightClick()) {
			ItemStack item = player.getInventory().getItemInMainHand();
			if (item.getType().isAir()) return;
			if (CurrencyManager.isMoney(item)) {
				int amount = 100 * item.getAmount();
				economyManager.deposit(uuid, amount);
				InfoUtils.info(player, "현금 &6{}원&f이 입금되었습니다.", amount);
				item.setAmount(0);
			}
		}
	}
}
