package org.lazberry.xmaslegacy.Utils;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;

@SuppressWarnings("unchecked")
public final class KeyUtils {

    @ApiStatus.Internal
    private KeyUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates new instance of NamespacedKey with intended value.
     * <pre>{@code
     * NamespacedKey key = KeyUtils.get("abc");
     * }</pre>
     * @param value what key to create
     * @return NamespacedKey of target value.
     */
    @Contract("_ -> new")
    public static @NotNull NamespacedKey get(@NotNull String value) {
        var plugin = XmasLegacy.getInstance();
        if (plugin == null) plugin = JavaPlugin.getPlugin(XmasLegacy.class);

        return new NamespacedKey(plugin, value);
    }

    @Contract("null, _, _ -> null")
    public static <V> @Nullable V get(@Nullable ItemStack item, @NotNull NamespacedKey key, @NotNull Class<V> clazz) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataType<?, V> type = getDataType(clazz);
        return meta.getPersistentDataContainer().get(key, type);
    }

    @Contract("null, _, _ -> null")
    public static <V> @Nullable V get(@Nullable ItemStack item, @NotNull NamespacedKey key, @NotNull PersistentDataType<?, V> type) {
        if (item == null) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(key, type);
    }

    @Contract("null, _, _ -> param3")
    public static <T> @NotNull T get(@Nullable ItemStack item, @NotNull NamespacedKey key, @NotNull T def) {
        T value = get(item, key, (Class<T>) def.getClass());
        return value == null ? def : value;
    }

    @Contract("null, _ -> false")
    public static boolean hasKey(@Nullable ItemStack item, @NotNull NamespacedKey key) {
        if (item == null) return false;
        var meta = item.getItemMeta();

        return meta != null
                && meta.getPersistentDataContainer().has(key);
    }

    private static <V> PersistentDataType<?, V> getDataType(Class<V> clazz) {
        if (clazz == String.class) return (PersistentDataType<?, V>) PersistentDataType.STRING;
        if (clazz == Integer.class || clazz == int.class) return (PersistentDataType<?, V>) PersistentDataType.INTEGER;
        if (clazz == Double.class || clazz == double.class) return (PersistentDataType<?, V>) PersistentDataType.DOUBLE;
        if (clazz == Float.class || clazz == float.class) return (PersistentDataType<?, V>) PersistentDataType.FLOAT;
        if (clazz == Long.class || clazz == long.class) return (PersistentDataType<?, V>) PersistentDataType.LONG;
        if (clazz == Short.class || clazz == short.class) return (PersistentDataType<?, V>) PersistentDataType.SHORT;
        if (clazz == Byte.class || clazz == byte.class) return (PersistentDataType<?, V>) PersistentDataType.BYTE;
        if (clazz == Boolean.class || clazz == boolean.class) return (PersistentDataType<?, V>) PersistentDataType.BOOLEAN;

        if (clazz == int[].class) return (PersistentDataType<?, V>) PersistentDataType.INTEGER_ARRAY;
        if (clazz == long[].class) return (PersistentDataType<?, V>) PersistentDataType.LONG_ARRAY;
        if (clazz == byte[].class) return (PersistentDataType<?, V>) PersistentDataType.BYTE_ARRAY;

        throw new IllegalArgumentException("지원하지 않는 PDC 데이터 타입입니다: " + clazz.getName());
    }

    public static <V> void set(@Nullable Entity entity, @NotNull NamespacedKey key, @NotNull V value) {
        if (entity == null) return;
        PersistentDataContainer container = entity.getPersistentDataContainer();
        PersistentDataType<?, V> type = getDataType((Class<V>) value.getClass());
        container.set(key, type, value);
    }

    public static <V> void set(@Nullable ItemStack item, @NotNull NamespacedKey key, @NotNull V value) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        var container = meta.getPersistentDataContainer();
        PersistentDataType<?, V> type = getDataType((Class<V>) value.getClass());
        container.set(key, type, value);

        item.setItemMeta(meta);
    }

    @Contract("null, _, _, _ -> false")
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
