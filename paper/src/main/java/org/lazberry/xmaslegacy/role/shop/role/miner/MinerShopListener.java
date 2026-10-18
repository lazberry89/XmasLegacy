package org.lazberry.xmaslegacy.role.shop.role.miner;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class MinerShopListener implements Listener {
	private final UserManager userManager;

	@Inject
	public MinerShopListener(UserManager userManager) {
		this.userManager = userManager;
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p)) return;

		var inv = e.getClickedInventory();
		if (inv == null) return;

		if (inv.getHolder() instanceof MinerSellingShop shop) {
			e.setCancelled(true);

			int slot = e.getRawSlot();
			if (slot == 0) {
				p.closeInventory();
				return;
			}
			if ((slot >= 10 && slot <= 16) || slot >=19 && slot <= 25)
				p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);

			User user = userManager.getUser(p.getUniqueId());
			shop.tryPurchase(p, user, slot, e.isShiftClick());
		}
	}
}
