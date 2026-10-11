package org.lazberry.xmaslegacy.role.shop.fisherman;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.user.UserManager;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class FishermanShopListener implements Listener {
	private final UserManager userManager;

	@Inject
	public FishermanShopListener(UserManager userManager) {
		this.userManager = userManager;
	}

	@EventHandler
	public void inventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p)) return;

		var inv = e.getClickedInventory();
		if (inv == null) return;

		if (inv.getHolder() instanceof FishSellingShop shop) {
			e.setCancelled(true);

			int slot = e.getRawSlot();
			if (slot != 1) p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);

			switch (slot) {
				case 0 -> p.closeInventory();
				case 2,3,4,5,6,7,8 -> {
					var user = userManager.getUser(p.getUniqueId());
					shop.tryPurchase(shop.getRecords(), p, user, slot, e.isShiftClick());
				}
			}
		}
	}
}
