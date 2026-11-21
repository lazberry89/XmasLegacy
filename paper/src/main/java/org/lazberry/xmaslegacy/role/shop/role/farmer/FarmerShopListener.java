package org.lazberry.xmaslegacy.role.shop.role.farmer;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;
import org.lazberry.xmaslegacy.role.shop.role.RoleShopManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.UserHandler;

@Components.Exclude(type = ServerType.LOBBY)
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

		// The shop inventories are shared by every player, so nothing may be moved into them.
		if (isFarmerShop(e.getView().getTopInventory().getHolder())) e.setCancelled(true);

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

			OraxenItemShop target = switch (slot) {
				case 2 -> farmer.getToolShop();
				case 4 -> farmer.getSeedShop();
				case 6 -> farmer.getCropShop();
				default -> null;
			};
			if (target != null) {
				p.openInventory(target.getInventory());
				p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
			}
			return;
		}
		if (holder instanceof OraxenItemShop shop) {
			e.setCancelled(true);

			if (slot == 0) {
				p.openInventory(farmer.getSelectionGui().getInventory());
				p.playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
				return;
			}
			var entry = shop.entry(slot);
			if (entry == null) return;

			if (shop.mode() == OraxenItemShop.Mode.BUY) shop.buy(p, userManager, entry);
			else shop.sell(p, user, entry, e.isShiftClick());
		}
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {
		if (isFarmerShop(e.getView().getTopInventory().getHolder())) e.setCancelled(true);
	}

	private static boolean isFarmerShop(InventoryHolder holder) {
		return holder instanceof FarmerSelectionGui || holder instanceof OraxenItemShop;
	}
}
