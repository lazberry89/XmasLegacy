package org.lazberry.xmaslegacy.utils;

import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.bags.BagManager;

import java.util.Objects;

public class InventoryHelper {
    private static @Setter BagManager bm;

    public static ItemStack background() {
        return ItemBuilder.of(XmasLegacy.getInstance(), Material.GRAY_STAINED_GLASS_PANE)
                .name(ColorUtils.chat(""))
                .hideAllFlags()
                .build();
    }

	public static ItemStack back() {
		return ItemBuilder.of(XmasLegacy.getInstance(), Material.BARRIER)
				.name(ColorUtils.chat("&c&l뒤로가기"))
				.lore(ColorUtils.chat("&7클릭하여 뒤로가기"))
				.hideAllFlags().build();
	}

    public static void giveItemOrDrop(@Nullable Player p, ItemStack... items) {
        if (items.length == 0 || p == null || !p.isOnline()) return;

        var loc = p.getLocation();
        var world = loc.getWorld();
        final var remains = p.getInventory().addItem(items);

        var itemList = remains.values();
        if (itemList.isEmpty()) return;

        itemList.stream()
                .filter(Objects::nonNull)
                .forEach(i -> world.dropItemNaturally(loc, i));
    }

    public static void giveItemOrKeep(@Nullable Player p, ItemStack... items) {
        if (items.length == 0 || p == null || !p.isOnline()) return;

        final var remains = p.getInventory().addItem(items);
        var remainItems = remains.values();
        if (remainItems.isEmpty()) return;

        bm.addAll(p, remainItems);
    }

    public static boolean hasItem(@Nullable Player p, ItemStack target, int amount) {
        if (p == null || !p.isOnline() || target == null || amount <= 0) return false;

        int count = 0;
        for (ItemStack item : p.getInventory().getStorageContents()) {
            if (item != null && item.isSimilar(target)) {
                count += item.getAmount();
                if (count >= amount) return true;
            }
        }
        return false;
    }

    public static boolean hasItem(@Nullable Player p, ItemStack target) {
        return target != null && hasItem(p, target, target.getAmount());
    }

    public static boolean removeItem(@Nullable Player p, ItemStack target, int amount) {
        if (!hasItem(p, target, amount)) return false;

        int remain = amount;
        var storage = p.getInventory().getStorageContents();

        for (int i = 0; i < storage.length; i++) {
            ItemStack item = storage[i];
            if (item == null || !item.isSimilar(target)) continue;

            remain = deductItemAmount(storage, i, remain);
            if (remain <= 0) break;
        }

        p.getInventory().setStorageContents(storage);
        return true;
    }

    public static boolean removeItem(@Nullable Player p, ItemStack target) {
        return target != null && removeItem(p, target, target.getAmount());
    }

    public static <V> boolean hasItemByTag(@Nullable Player p, NamespacedKey key, PersistentDataType<?, V> type, V tagValue, int amount) {
        if (p == null || !p.isOnline() || amount <= 0) return false;
        int count = 0;
        for (ItemStack item : p.getInventory().getStorageContents()) {
            if (item != null && item.hasItemMeta()) {
                V val = KeyUtils.get(item, key, type);
                if (tagValue.equals(val)) {
                    count += item.getAmount();
                    if (count >= amount) return true;
                }
            }
        }
        return false;
    }

    public static <V> boolean removeItemByTag(@Nullable Player p, NamespacedKey key, PersistentDataType<?, V> type, V tagValue, int amount) {
        if (!hasItemByTag(p, key, type, tagValue, amount)) return false;
        int remain = amount;
        var storage = p.getInventory().getStorageContents();

        for (int i = 0; i < storage.length; i++) {
            ItemStack item = storage[i];
            if (item == null || !item.hasItemMeta()) continue;

            V val = KeyUtils.get(item, key, type);
            if (!tagValue.equals(val)) continue;

            remain = deductItemAmount(storage, i, remain);
            if (remain <= 0) break;
        }

        p.getInventory().setStorageContents(storage);
        return true;
    }

    private static int deductItemAmount(ItemStack[] storage, int index, int remain) {
        ItemStack item = storage[index];
        int count = item.getAmount();

        if (count <= remain) {
            remain -= count;
            storage[index] = null;
        } else {
            item.setAmount(count - remain);
            remain = 0;
        }
        return remain;
    }

	public static boolean hasItem(@Nullable Player p, Material material, int amount) {
		if (p == null || !p.isOnline() || material == null || material.isAir() || amount <= 0) return false;

		int count = 0;
		for (ItemStack item : p.getInventory().getStorageContents()) {
			if (item != null && item.getType() == material) {
				count += item.getAmount();
				if (count >= amount) return true;
			}
		}
		return false;
	}

	public static boolean hasItem(@Nullable Player p, Material material) {
		return hasItem(p, material, 1);
	}

	public static boolean removeItem(@Nullable Player p, Material material, int amount) {
		if (!hasItem(p, material, amount)) return false;

		int remain = amount;
		var storage = p.getInventory().getStorageContents();

		for (int i = 0; i < storage.length; i++) {
			ItemStack item = storage[i];
			if (item == null || item.getType() != material) continue;

			remain = deductItemAmount(storage, i, remain);
			if (remain <= 0) break;
		}

		p.getInventory().setStorageContents(storage);
		return true;
	}

	public static boolean removeItem(@Nullable Player p, Material material) {
		return removeItem(p, material, 1);
	}

	public static int getItemCount(@Nullable Player p, Material material) {
		if (p == null || !p.isOnline() || material == null || material.isAir()) return 0;

		int count = 0;
		for (ItemStack item : p.getInventory().getStorageContents()) {
			if (item != null && item.getType() == material) {
				count += item.getAmount();
			}
		}
		return count;
	}

	public static int getItemCount(@Nullable Player p, ItemStack target) {
		if (p == null || !p.isOnline() || target == null) return 0;

		int count = 0;
		for (ItemStack item : p.getInventory().getStorageContents()) {
			if (item != null && item.isSimilar(target)) {
				count += item.getAmount();
			}
		}
		return count;
	}
}
