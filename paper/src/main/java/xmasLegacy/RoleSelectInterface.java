package xmasLegacy;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.Utils.ItemBuilder;

@SuppressWarnings("FieldCanBeLocal")
public class RoleSelectInterface implements InventoryHolder {
	private final XmasLegacy plugin;
	private final Inventory inv;

	public RoleSelectInterface(XmasLegacy plugin) {
		this.plugin = plugin;
		this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&c&l역할 선택"));
		ItemStack warrior = ItemBuilder.of(plugin, Material.IRON_AXE)
				.setName(ColorUtils.chat("&c&l전사"))
				.setLore(ColorUtils.chat("&7강력한 공격력과 체력을 자랑하는 역할입니다."))
				.hideAllFlags()
				.build();
		ItemStack rogue = ItemBuilder.of(plugin, Material.IRON_BOOTS)
				.setName(ColorUtils.chat("&c&l도적"))
				.setLore(ColorUtils.chat("&7빠른 움직임과 은신 능력을 가지고 있습니다."))
				.hideAllFlags()
				.build();
		ItemStack mage = ItemBuilder.of(plugin, Material.BLAZE_ROD)
				.setName(ColorUtils.chat("&c&l마법사"))
				.setLore(ColorUtils.chat("&7강력한 범위공격이 특징입니다."))
				.hideAllFlags()
				.build();
		ItemStack knight = ItemBuilder.of(plugin, Material.IRON_SWORD)
				.setName(ColorUtils.chat("&c&l기사"))
				.setLore(ColorUtils.chat("&7모든방면에서 6각형의 스탯을 자랑합니다."))
				.hideAllFlags()
				.build();
		ItemStack archer = ItemBuilder.of(plugin, Material.BOW)
				.setName(ColorUtils.chat("&c&궁수"))
				.setLore(ColorUtils.chat("&7원거리에서 강력한 공격을 가할 수 있습니다."))
				.hideAllFlags()
				.build();
		ItemStack priest = ItemBuilder.of(plugin, Material.GOLDEN_SPEAR)
				.setName(ColorUtils.chat("&c&l성직자"))
				.setLore(ColorUtils.chat("&7팀원들을 회복, 버프시켜주며 전투를 지원합니다."), ColorUtils.chat("&7또한 전투물약판매를 지원합니다."))
				.hideAllFlags()
				.build();
		ItemStack miner = ItemBuilder.of(plugin, Material.GOLDEN_PICKAXE)
				.setName(ColorUtils.chat("&c&l광부"))
				.setLore(ColorUtils.chat("&7채굴과 광물 수집에 특화되어 있습니다."))
				.hideAllFlags()
				.build();
		ItemStack merchant = ItemBuilder.of(plugin, Material.ENDER_CHEST)
				.setName(ColorUtils.chat("&c&l상인"))
				.setLore(ColorUtils.chat("&7세계관의 경제흐름을 만드는 역할입니다"))
				.hideAllFlags()
				.build();
		ItemStack gatherer = ItemBuilder.of(plugin, Material.COMPASS)
				.setName(ColorUtils.chat("&c&l수집가"))
				.setLore(ColorUtils.chat("&7이터널 포스를 사용하여 위치를 기록하고"), ColorUtils.chat("&7유물을 수집합니다."))
				.hideAllFlags()
				.build();
		ItemStack farmer = ItemBuilder.of(plugin, Material.IRON_HOE)
				.setName(ColorUtils.chat("&c&l농부"))
				.setLore(ColorUtils.chat("&7모든 이들이 얼어붙지않게, 굶지않게 해줍니다."))
				.hideAllFlags()
				.build();
	}

	@Override
	public @NotNull Inventory getInventory() {
		return this.inv;
	}
}
