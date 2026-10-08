package org.lazberry.xmaslegacy.enchant.buffer;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.enchant.EnchantManager;
import org.lazberry.xmaslegacy.enchant.material.EnchantMaterial;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.function.Consumer;

public class EnchantBuffHandler {

    public static boolean setBuffed(@Nullable ItemStack item, double amount) {
        if (isBuffedTool(item)) return false;
        if (!KeyUtils.hasKey(item, EnchantManager.key)) return false;
        KeyUtils.set(item, EnchantMaterial.bufferKey, amount);
        return true;
    }

    public static boolean isBuffer(ItemStack item) {
        return KeyUtils.hasKey(item, EnchantMaterial.key, PersistentDataType.STRING, "booster_five_percent") ||
                KeyUtils.hasKey(item, EnchantMaterial.key, PersistentDataType.STRING, "booster_ten_percent");
    }

    public static double getBufferAmount(@Nullable ItemStack item) {
        if (KeyUtils.hasKey(item, EnchantMaterial.key, PersistentDataType.STRING, "booster_five_percent")) {
            return 5.0;
        }
        if (KeyUtils.hasKey(item, EnchantMaterial.key, PersistentDataType.STRING, "booster_ten_percent")) {
            return 10.0;
        }
        return 0.0;
    }

    public static boolean isBuffedTool(@Nullable ItemStack item) {
        return KeyUtils.hasKey(item, EnchantMaterial.bufferKey);
    }

    public static boolean checkIfBuffedAndGet(@Nullable ItemStack item, Consumer<Double> buff) {
        double value = KeyUtils.get(item, EnchantMaterial.bufferKey, 0.0);
        if (value == 0.0) return false;
        buff.accept(value);
        return true;
    }

    public static boolean removeBuffTag(@Nullable ItemStack item) {
        if (!isBuffedTool(item)) return false;
        KeyUtils.remove(item, EnchantMaterial.bufferKey);
        return true;
    }
}
