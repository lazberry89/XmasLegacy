package org.lazberry.xmaslegacy.blueprint.shop;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.blueprint.BluePrint;
import org.lazberry.xmaslegacy.blueprint.BluePrintManager;
import org.lazberry.xmaslegacy.blueprint.BlueprintGrade;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;

@Listeners
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BlueprintShopListener implements Listener {
	private final BluePrintManager blueprintManager;
	private final UserManager userManager;

	@Inject
	public BlueprintShopListener(BluePrintManager blueprintManager, UserManager userManager) {
		this.blueprintManager = blueprintManager;
		this.userManager = userManager;
	}

	@EventHandler
	public void InventoryClickEvent(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player p)) return;
		Inventory inv = event.getClickedInventory();

		if (inv == null || !(inv.getHolder() instanceof BlueprintShop shop)) return;

		int slot = event.getRawSlot();
		event.setCancelled(true);

		switch (slot) {
			case 47 -> {
				shop.changePage(BlueprintGrade.LOW);
				p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
				p.updateInventory();
			}
			case 49 -> {
				shop.changePage(BlueprintGrade.MEDIUM);
				p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
				p.updateInventory();
			}
			case 51 -> {
				shop.changePage(BlueprintGrade.HIGH);
				p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
				p.updateInventory();
			}
			default -> {
				var item = event.getCurrentItem();
				if (item == null || item.getType().isAir()) return;
				if (!BluePrint.isBluePrint(item)) {
					p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
					return;
				}
				var name = BluePrint.getNameByItem(item);
				if (name == null) return;
				blueprintManager.getBluePrint(name).ifPresent(b -> {
					var uuid = p.getUniqueId();
					int price = b.getPrice();
					if (userManager.withdraw(uuid, price)) {
						p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
						InventoryHelper.giveItemOrDrop(p, b.getItem());
					} else InfoUtils.error(p, "잔액이 부족하여 구매에 실패하였습니다.");
				});
			}
		}
	}
}
