package xmasLegacy.Enchant;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.Objects;

public class EnchantUserInterface implements InventoryHolder {
    private final Inventory inv;
    private final EnchantManager ecm;
    private final XmasLegacy plugin;

    public EnchantUserInterface() {
        this.ecm = EnchantManager.INSTANCE;
        this.plugin = XmasLegacy.getInstance();
        this.inv = Bukkit.createInventory(this, 27, ColorUtils.chat("&c&l[ 장비 강화 ]"));
        ItemStack bg = ItemBuilder.of(plugin, Material.GRAY_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat(""))
                .hideAllFlags()
                .build().clone();
        for (int i = 0; i < this.inv.getSize(); i++) this.inv.setItem(i, bg);
        this.inv.setItem(13, new ItemStack(Material.AIR));
        this.inv.setItem(22, makeButton(null));
    }

    public @NotNull ItemStack makeButton(@Nullable ItemStack item) {
        if (item == null || !ecm.isEnchantable(item)) return ItemBuilder.of(plugin, Material.ENCHANTING_TABLE)
                .setName(ColorUtils.chat("&6&l[ 강화하기 ]"))
                .setLore(ColorUtils.chat("&7&k######"))
                .hideAllFlags()
                .setGlint(false)
                .build().clone();
        else {
            int lvl = Objects.requireNonNullElse(ecm.getEnchantLevel(item), 1);
            int needed = Constants.ENCHANT_NEEDED.get(lvl - 1);
            var chance = ecm.getChanceInfo(lvl);
            return ItemBuilder.of(plugin, Material.ENCHANTING_TABLE)
                    .setName(ColorUtils.chat("&6&l[ 강화하기 ]"))
                    .setLore(
                            ColorUtils.chat("&7현재등급 : "),
                            ecm.getLore(lvl),
                            Component.empty(),
                            ColorUtils.chat(String.format("&a%.1f%% &f| &c%.1f%% &f| &4%.1f%%", chance.success(), chance.fail(), chance.breakChance())),
                            Component.empty(),
                            ColorUtils.chat(String.format("&7필요 개수 : %d", needed))
                    )
                    .hideAllFlags()
                    .setGlint(true)
                    .build().clone();
        }
    }

    public void updateInv(Player view) {
        this.inv.setItem(22, makeButton(this.inv.getItem(13)));
        view.updateInventory();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
}
