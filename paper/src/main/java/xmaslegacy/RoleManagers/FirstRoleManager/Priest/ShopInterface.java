package xmaslegacy.RoleManagers.FirstRoleManager.Priest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmaslegacy.Utils.ItemBuilder;
import xmaslegacy.XmasLegacy;

public class ShopInterface implements InventoryHolder {
	private final Inventory inv;
	private final PriestShop PSP;

	public ShopInterface(PriestShop PSP) {
		this.PSP = PSP;
		this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&6&l성직자의 상점"));
		ItemStack bg = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.GRAY_STAINED_GLASS_PANE).setName(ColorUtils.chat("")).setLore(ColorUtils.chat("")).hideAllFlags().build();
		ItemStack nonStock1 = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.BARRIER)
				.setName(ColorUtils.chat("&d&l용의 숨결"))
				.setLore(ColorUtils.chat("&c&l재고가 떨어졌습니다!"), ColorUtils.chat("&6&l재고 :&c " + PSP.getDragonStock()))
				.hideAllFlags()
				.build()
				.clone();
		ItemStack nonStock2 = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.BARRIER)
				.setName(ColorUtils.chat("&e&l회복 포션"))
				.setLore(ColorUtils.chat("&c&l재고가 떨어졌습니다!"), ColorUtils.chat("&6&l재고 :&c " + PSP.getHealerStock()))
				.hideAllFlags()
				.build()
				.clone();
		ItemStack nonStock3 = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.BARRIER)
				.setName(ColorUtils.chat("&b&l보호의 물약"))
				.setLore(ColorUtils.chat("&c&l재고가 떨어졌습니다!"), ColorUtils.chat("&6&l재고 :&c " + PSP.getProtectionStock()))
				.hideAllFlags()
				.build()
				.clone();
		ItemStack nonStock4 = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.BARRIER)
				.setName(ColorUtils.chat("&e&l스피어탄"))
				.setLore(ColorUtils.chat("&c&l재고가 떨어졌습니다!"), ColorUtils.chat("&6&l재고 :&c " + PSP.getSpearStock()))
				.hideAllFlags()
				.build()
				.clone();
		ItemStack nonStock5 = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.BARRIER)
				.setName(ColorUtils.chat("&4&l???"))
				.setLore(ColorUtils.chat("&c&l재고가 떨어졌습니다!"), ColorUtils.chat("&6&l재고 :&c " + PSP.getSaveStock()))
				.hideAllFlags()
				.build()
				.clone();
		for (int i = 0; i < this.inv.getSize(); i++) this.inv.setItem(i, bg);
		this.inv.setItem(2, PSP.getDragonStock() == 0 ? nonStock1 : ConductableItems.DragonPotion());
		this.inv.setItem(3, PSP.getHealerStock() == 0 ? nonStock2 : ConductableItems.HealerPotion());
		this.inv.setItem(4, PSP.getProtectionStock() == 0 ? nonStock3 : ConductableItems.ProtectionPotion());
		this.inv.setItem(5, PSP.getSpearStock() == 0 ? nonStock4 : ConductableItems.SpearPotion());
		this.inv.setItem(6, PSP.getSaveStock() == 0 ? nonStock5 : ConductableItems.DeathSave());
	}

	@Override
	public @NotNull Inventory getInventory() {
		return this.inv;
	}
	public PriestShop getShop() {return PSP;}
	public Player getOwner() {return PSP.getOwner();}
}
