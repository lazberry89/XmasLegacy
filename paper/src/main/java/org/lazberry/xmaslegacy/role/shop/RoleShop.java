package org.lazberry.xmaslegacy.role.shop;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.UserHandler;

public abstract class RoleShop {

	public record ShopRecord(Material material, int price) {}

	public void tryPurchase(ShopRecord[] recordArray, @Nullable Player p, @Nullable User user, int slot, boolean shift) {
		if (p == null) return;
		if (user == null) {
			UserHandler.loadUser(p, true);
			return;
		}
		var record = recordArray[slot];
		if (record == null) return;

		Material material = record.material;
		int price = record.price;
		int amount = 1;

		if (shift) amount = InventoryHelper.getItemCount(p, material);
		if (amount <= 0) return;

		if (InventoryHelper.removeItem(p, material, amount)) {
			p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			user.addDollars(price * amount);
		} else p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
	}
}
