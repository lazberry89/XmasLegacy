package org.lazberry.xmaslegacy.enchant;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.FloodgateUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

import java.util.Objects;

public class EnchantUserInterface implements InventoryHolder {
    private final @NotNull Inventory inv;
    private final @NotNull EnchantManager ecm;
    private final @NotNull XmasLegacy plugin;
    private final boolean customGui;

    /** Font in XmasResourcePack: U+F801 shifts -8px onto the window edge, U+E001 is the forge art over the top (container) part. */
    private static final Key GUI_FONT = Key.key("xmaslegacy", "gui");

    public EnchantUserInterface(@NotNull EnchantManager ecm, @NotNull Player viewer) {
        this.ecm = ecm;
        this.plugin = XmasLegacy.getInstance();

        // Bedrock clients don't get the Java resource pack: keep the plain title and glass-pane look for them.
        this.customGui = !FloodgateUtils.isFloodgate(viewer);
        this.inv = Bukkit.createInventory(this, 27, customGui ? customTitle() : ColorUtils.chat("&c&l[ 장비 강화 ]"));

        if (!customGui) {
            ItemStack bg = ItemBuilder.of(plugin, Material.GRAY_STAINED_GLASS_PANE)
                    .name(ColorUtils.chat(""))
                    .hideAllFlags()
                    .build().clone();
            for (int i = 0; i < this.inv.getSize(); i++) this.inv.setItem(i, bg);
            this.inv.setItem(13, new ItemStack(Material.AIR));
        }
        // With the custom window the other slots stay empty: items there would be drawn over the artwork.
        placeButton(makeButton(null));
    }

    /** Animated Oraxen glyph (XmasResourcePack/oraxen): -8 shift onto the window edge, then the 16-frame forge art. */
    private static final String ORAXEN_TITLE = "<white><shift:-8><glyph:enchant_forge>";

    private static @NotNull Component customTitle() {
        if (Bukkit.getPluginManager().isPluginEnabled("Oraxen"))
            return ColorUtils.minimessage(ORAXEN_TITLE);
        return packTitle();
    }

    /** Fallback without Oraxen: static art from the xmaslegacy:gui font in XmasResourcePack. */
    private static @NotNull Component packTitle() {
        // No text title: the art replaces it. White keeps the glyph's real colors (the default title color tints it).
        return Component.text("", NamedTextColor.WHITE).font(GUI_FONT);
    }

	public @NotNull ItemStack makeButton(@Nullable ItemStack item) {
		if (item == null || !ecm.isEnchantable(item)) return ItemBuilder.of(plugin, Material.ENCHANTING_TABLE)
				.name(ColorUtils.chat("&6&l[ 강화하기 ]"))
				.lore(ColorUtils.chat("&7&k######"))
				.hideAllFlags()
				.setGlint(false)
				.build().clone();
		else {
			int lvl = Objects.requireNonNullElse(ecm.getEnchantLevel(item), 1);

			if (lvl - 1 >= Constants.ENCHANT_NEEDED.size()) {
				return ItemBuilder.of(plugin, Material.ENCHANTING_TABLE)
						.name(ColorUtils.chat("&6&l[ 강화하기 ]"))
						.lore(
								ColorUtils.chat("&7현재등급 : "),
								ecm.getLore(lvl),
								Component.empty(),
								ColorUtils.chat("&c최대 등급에 도달했습니다!")
						)
						.hideAllFlags()
						.setGlint(false)
						.build().clone();
			}

			int needed = Constants.ENCHANT_NEEDED.get(lvl - 1);
			var chance = ecm.getChanceInfo(lvl);
			return ItemBuilder.of(plugin, Material.ENCHANTING_TABLE)
					.name(ColorUtils.chat("&6&l[ 강화하기 ]"))
					.lore(
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

    /** The forge art already draws the button: hide the item model (vanilla minecraft:air renders nothing) but keep its tooltip. */
    private void placeButton(@NotNull ItemStack button) {
        if (customGui) button.editMeta(m -> m.setItemModel(NamespacedKey.minecraft("air")));
        this.inv.setItem(22, button);
    }

    public void updateInv(@NotNull Player view) {
        placeButton(makeButton(this.inv.getItem(13)));
        view.updateInventory();
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
}
