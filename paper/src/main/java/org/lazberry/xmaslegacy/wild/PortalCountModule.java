package org.lazberry.xmaslegacy.wild;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.concurrent.ThreadLocalRandom;

@Components.Include(type = ServerType.WILD)
public class PortalCountModule {
    private static final NamespacedKey key = KeyUtils.get("module");
    private static ItemStack item = null;

    private final XmasLegacy plugin;

    @Inject
    public PortalCountModule(XmasLegacy plugin) {
        this.plugin = plugin;
    }

    public ItemStack getItem() {
        if (item == null) {
            var builder = OraxenItems.getItemById("count_module");
            var itemStack = builder == null ? new ItemStack(Material.STICK) : builder.build();

            var result = ItemBuilder.of(plugin, itemStack)
                    .name(ColorUtils.chat("&d차원 이동 제한 모듈"))
                    .lore(ColorUtils.chat(""))
                    .setMaxStackSize(1)
                    .glint(true)
                    .hideAllFlags()
                    .setTag(key, "portal_module")
                    .build();
            ItemMeta meta = result.getItemMeta();
            if (meta instanceof Damageable d) {
                d.setMaxDamage(100);
                d.setDamage(0);
                result.setItemMeta(d);
            }
            item = result;
        }
        return item;
    }

    public static boolean isModule(ItemStack item) {
        return KeyUtils.hasKey(item, key, PersistentDataType.STRING, "portal_module");
    }

    public boolean useModule(Player p, ItemStack item) {
        if (!isModule(item)) return false;

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable d)) return false;
        if (d.getDamage() >= d.getMaxDamage()) return false;

        var world = p.getWorld();
        var loc = p.getLocation();

        int next = ThreadLocalRandom.current().nextInt(5, 11);
        d.setDamage(d.getDamage() + next);

        if (d.getDamage() >= d.getMaxDamage()) {
            item.setAmount(0);
            world.playSound(loc, Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            InfoUtils.error(p, "포탈 모듈이 파괴되었습니다.");
        }

        p.sendActionBar(ColorUtils.chat("&d모듈 사용됨: -" + next));
        world.playSound(p, Sound.BLOCK_GRINDSTONE_USE, 0.5f, 1.0f);
        world.playSound(p, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.5f, 1.0f);

        item.setItemMeta(d);
        return true;
    }
}
