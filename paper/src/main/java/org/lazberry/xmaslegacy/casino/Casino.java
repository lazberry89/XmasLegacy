package org.lazberry.xmaslegacy.casino;

import io.th0rgal.oraxen.api.OraxenItems;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.bags.BagManager;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public final class Casino {
	public static final Component icon =
			ColorUtils.chat("&#FF4545[&#E34444C&#C74242a&#AB4141s&#8F3F3Fi&#733E3En&#573C3Co&#3B3B3B]");
    private static @Setter BagManager bm;

    public static NamespacedKey key() {
        return KeyUtils.get("casino");
    }

    public static boolean isCoin(ItemStack estimated) {
        return KeyUtils.hasKey(estimated, key(), PersistentDataType.STRING, "coin");
    }

    public static ItemStack coin(int amount) {
        var builder = OraxenItems.getItemById("casino_coin");
        ItemStack item = builder == null ? new ItemStack(Material.IRON_NUGGET) : builder.build();

        return ItemBuilder.of(XmasLegacy.getInstance(), item)
                .setName(ColorUtils.chat("&6&l카지노 코인"))
                .setLore(ColorUtils.chat("&7카지노의 베팅시스템에 사용되는 코인입니다."))
                .setMaxStackSize(16)
                .setTag(key(), "coin")
                .setAmount(amount)
                .build();
    }

    public static boolean applyCoin(User user, int amount) {
        if (user == null || amount <= 0) return false;

        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player == null || !player.isValid()) return false;

        int neededMoney = 10_000 * amount;
        if (user.getDollars() < neededMoney) return false;
        user.addDollars(-neededMoney);

        Map<Integer, ItemStack> leftOver = player.getInventory().addItem(coin(amount));
        if (!leftOver.isEmpty()) {
            leftOver.values().forEach(i -> bm.addItem(player, i));
        }
        return true;
    }

	public static int countCoins(Player player) {
		return Arrays.stream(player.getInventory().getContents())
				.filter(Objects::nonNull)
				.filter(i -> !i.getType().isAir())
				.filter(Casino::isCoin)
				.mapToInt(ItemStack::getAmount)
				.sum();
	}

	public static boolean hasEnoughCoins(Player player, int amount) {
		return countCoins(player) >= amount;
	}

	public static void setCoins(Player player, int targetAmount) {
		if (player == null || targetAmount < 0) return;

		int currentCount = countCoins(player);
		if (currentCount == targetAmount) return;

		if (currentCount > targetAmount) {
			// 목표량보다 많으면 초과분만큼 차감
			removeCoins(player, currentCount - targetAmount);
		} else {
			// 목표량보다 적으면 부족분만큼 추가 (인벤토리 가득 찰 시 가방 오버플로우 처리)
			int toAdd = targetAmount - currentCount;
			Map<Integer, ItemStack> leftOver = player.getInventory().addItem(coin(toAdd));
			if (!leftOver.isEmpty() && bm != null) {
				leftOver.values().forEach(i -> bm.addItem(player, i));
			}
		}
	}

	public static void removeCoins(Player player, int amount) {
		if (player == null || amount <= 0) return;

		int left = amount;
		var inv = player.getInventory();
		for (int i = 0; i < inv.getSize(); i++) {
			var item = inv.getItem(i);
			if (isCoin(item)) {
				if (item.getAmount() <= left) {
					left -= item.getAmount();
					inv.setItem(i, null);
				} else {
					item.setAmount(item.getAmount() - left);
					break;
				}
			}
			if (left <= 0) break;
		}
	}

	public static void sendIconAlert(Player player, String message) {
		player.sendMessage(icon.appendSpace().append(ColorUtils.chat(message)));
	}
}
