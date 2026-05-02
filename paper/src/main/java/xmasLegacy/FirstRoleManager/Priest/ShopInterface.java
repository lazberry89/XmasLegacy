package xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

public class ShopInterface implements InventoryHolder {
	private final Inventory inv;
	private final

	public ShopInterface() {
		this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&6&l성직자의 상점"));
		ItemStack bg = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.GRAY_STAINED_GLASS_PANE).setName(ColorUtils.chat("")).setLore(ColorUtils.chat("")).hideAllFlags().build()
		for (int i = 0; i < this.inv.getSize(); i++) this.inv.setItem(i, bg);
	}

	@Override
	public @NotNull Inventory getInventory() {
		return this.inv;
	}
}
