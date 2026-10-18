package org.lazberry.xmaslegacy.role.shop.role.farmer;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.role.shop.role.RoleShopManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.UserHandler;

@Registry.Exclude(type = ServerType.LOBBY)
public class FarmerShopListener implements Listener {
	private final RoleShopManager roleShopManager;
	private final UserManager userManager;

	@Inject
	public FarmerShopListener(RoleShopManager roleShopManager, UserManager userManager) {
		this.roleShopManager = roleShopManager;
		this.userManager = userManager;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		var farmer = roleShopManager.farmer();

		if (!(e.getWhoClicked() instanceof Player p)) return;

		var inv = e.getClickedInventory();
		if (inv == null) return;

		InventoryHolder holder = inv.getHolder();
		int slot = e.getRawSlot();

		var uuid = p.getUniqueId();
		User user = userManager.getUser(uuid);
		if (user == null) {
			InfoUtils.error(p, "유저 정보가 로드되지 않았습니다.");
			UserHandler.loadUser(p, false);
			return;
		}

		if (holder instanceof FarmerSelectionGui) {
			e.setCancelled(true);

			if (slot == 2) {
				p.openInventory(farmer.getSellingShop().getInventory());
				p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
			} else if (slot == 6) {
				p.openInventory(farmer.getPurchaseShop().getInventory());
				p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
			}
		}
		if (holder instanceof CropSellingShop shop) {
			e.setCancelled(true);
			if (slot != 1) p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);

			switch (slot) {
				case 0 -> p.openInventory(farmer.getSelectionGui().getInventory());
				case 2, 3, 4, 5, 6, 7, 8 ->
					shop.tryPurchase(p, user, slot, e.isShiftClick());
			}
		}
		if (holder instanceof FarmPurchaseShop shop) {
			e.setCancelled(true);

			if (slot == 0) {
				p.openInventory(farmer.getSelectionGui().getInventory());
				p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
				return;
			}

			var record = shop.getRecord(slot);
			if (record != null) {
				p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
				Material material = record.material();
				int price = record.price();

				if (userManager.withdraw(uuid, price)) {
					InventoryHelper.giveItemOrDrop(p, new ItemStack(material));
					InfoUtils.info(p, "구매하였습니다.");
				} else InfoUtils.error(p, "잔액이 부족합니다.");
			}
		}
	}
}
