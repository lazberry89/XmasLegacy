package org.lazberry.xmaslegacy.Utils;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;

public final class KeyUtils {

    @ApiStatus.Internal
    private KeyUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static @NotNull NamespacedKey get(@NotNull String value) {
        var plugin = XmasLegacy.getInstance();
        if (plugin == null) plugin = JavaPlugin.getPlugin(XmasLegacy.class);

        return new NamespacedKey(plugin, value);
    }

    public static boolean hasKey(@Nullable ItemStack item, @NotNull NamespacedKey key) {
        if (item == null) return false;

        var meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(key);
    }

    public static <T, V> boolean hasKey(@Nullable ItemStack item, @NotNull NamespacedKey key, PersistentDataType<T, V> type, V value) {
        if (item == null) return false;

        var meta = item.getItemMeta();
        if (meta == null) return false;

        var container = meta.getPersistentDataContainer();

        if (!container.has(key, type)) return false;

        V actualValue = container.get(key, type);
        return value.equals(actualValue);
    }
}
