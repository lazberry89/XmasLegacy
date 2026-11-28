package org.lazberry.xmaslegacy.role.shop.role;

import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.advancement.Advancements;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.utils.UserHandler;

public abstract class RoleShop {
	private final ShopRecord[] records = new ShopRecord[54];

	public record ShopRecord(Material material, int price) {}

	protected void setRecord(int slot, ShopRecord record) {
		records[slot] = record;
	}

	/** Custom items share their material with ordinary ones; they must not be sold as those. */
	private static boolean isPlain(@Nullable ItemStack item, Material material) {
		return item != null && item.getType() == material && item.getPersistentDataContainer().isEmpty();
	}

	private static int countPlain(Player p, Material material) {
		int count = 0;
		for (ItemStack item : p.getInventory().getStorageContents())
			if (isPlain(item, material)) count += item.getAmount();
		return count;
	}

	private static boolean removePlain(Player p, Material material, int amount) {
		if (countPlain(p, material) < amount) return false;
		var contents = p.getInventory().getStorageContents();
		for (int i = 0; i < contents.length && amount > 0; i++) {
			if (!isPlain(contents[i], material)) continue;
			int taken = Math.min(amount, contents[i].getAmount());
			amount -= taken;
			contents[i] = taken == contents[i].getAmount() ? null : contents[i].asQuantity(contents[i].getAmount() - taken);
		}
		p.getInventory().setStorageContents(contents);
		return true;
	}

	public void tryPurchase(@Nullable Player p, @Nullable User user, int slot, boolean shift) {
		if (p == null) return;
		if (user == null) {
			UserHandler.loadUser(p, true);
			return;
		}
		var record = records[slot];
		if (record == null) return;

		Material material = record.material;
		int price = record.price;
		int amount = 1;

		if (shift) amount = countPlain(p, material);
		if (amount <= 0) return;
		if (removePlain(p, material, amount)) {
			p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			user.addDollars((long) price * amount);
			Advancements.grant(p, "earn_money");
			if (user.hasRole()) Advancements.grant(p, "use_role_shop");
		} else p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
	}
}
