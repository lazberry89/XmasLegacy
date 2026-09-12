package org.lazberry.xmaslegacy.casino;

import io.th0rgal.oraxen.api.OraxenItems;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.playerUtils.bags.BagManager;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.Map;

public final class Casino {
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
}
