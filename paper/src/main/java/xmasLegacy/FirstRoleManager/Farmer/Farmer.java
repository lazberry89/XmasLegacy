package xmasLegacy.FirstRoleManager.Farmer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Prefix;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.Region.Region;
import xmasLegacy.Region.RegionManager;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;

public class Farmer extends AbstractFirstRole {
	private final RegionManager rm;

	public Farmer(int c1, int c2, XmasLegacy plugin, RegionManager rm) {
		super(c1, c2, plugin);
		this.rm = rm;
	}

	@Override
	public void useFirstSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool.getType()) / 20 + "초 기다리세요"));
			return;
		}

		List<Region> playerRegions = rm.getRegion(p);
		if (playerRegions == null || playerRegions.isEmpty()) {
			return;
		}

		List<Block> crops = getFullyGrownCrops(p, 4);
		for (Block block : crops) {
			Region cropRegion = rm.getRegionAt(block.getLocation());

			if (cropRegion != null && playerRegions.contains(cropRegion)) {
				block.breakNaturally();
			}
		}

		p.setCooldown(tool, this.getCooldown1() * 20);
	}

	public List<Block> getFullyGrownCrops(Player player, int radius) {
		List<Block> grownCrops = new ArrayList<>();
		Location center = player.getLocation();

		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {

					Block block = center.clone().add(x, y, z).getBlock();

					if (block.getBlockData() instanceof Ageable ageable) {
						if (ageable.getAge() == ageable.getMaximumAge()) {
							grownCrops.add(block);
						}
					}
				}
			}
		}
		return grownCrops;
	}

	@Override
	public void useSecondSkill(Player player) {

	}

	@Override
	public Roles getRole() {
		return null;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.IRON_HOE)
				.setName(ColorUtils.chat("&e&l눙부의 낫"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setTag("role_id", "farmer")
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), Material.LEATHER_CHESTPLATE)
				.setName(ColorUtils.chat("&e&l조끼"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setArmorState(5.0, EquipmentSlotGroup.CHEST)
				.setTag("role_id", "farmer")
				.build()
				.clone();
	}
}
