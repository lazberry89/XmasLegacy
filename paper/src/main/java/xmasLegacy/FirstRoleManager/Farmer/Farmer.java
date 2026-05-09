package xmasLegacy.FirstRoleManager.Farmer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.Region.Region;
import xmasLegacy.Region.RegionManager;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.*;

@SuppressWarnings("DuplicatedCode")
public class Farmer extends AbstractFirstRole {
	private final RegionManager rm;
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.RADIUS_HARVEST);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

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
		if (!consumeEnergy(p, 3)) return;
		List<Region> playerRegions = rm.getRegion(p);
		if (playerRegions == null || playerRegions.isEmpty()) {
			return;
		}

		List<Block> crops = getFullyGrownCrops(p, 4);
		for (Block block : crops) {
			Region cropRegion = rm.getRegionAt(block.getLocation());

			if (cropRegion != null && playerRegions.contains(cropRegion)) {
				block.breakNaturally();
				block.getLocation().getWorld().playSound(block.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
			} else {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 적절한 사용 조건이 아닙니다."));
				p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
				return;
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
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;

		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool.getType()) / 20 + "초 기다리세요"));
			return;
		}
		List<Region> playerRegions = rm.getRegion(p);
		if (playerRegions == null || playerRegions.isEmpty()) return;

		int radius = 4;
		Location center = p.getLocation();
		boolean success = false;

		for (int x = -radius; x <= radius; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -radius; z <= radius; z++) {
					Block block = center.clone().add(x, y, z).getBlock();

					if (block.getBlockData() instanceof Ageable ageable) {
						Region cropRegion = rm.getRegionAt(block.getLocation());

						if (cropRegion != null && playerRegions.contains(cropRegion) && ageable.getAge() < ageable.getMaximumAge()) {
							ageable.setAge(ageable.getMaximumAge());
							block.setBlockData(ageable);
							block.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.5, 0.5), 5, 0.2, 0.2, 0.2);
							success = true;
						} else {
							p.sendMessage(ColorUtils.chat(Prefix.RED + " 적절한 사용 조건이 아닙니다."));
							p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
							return;
						}
					}
				}
			}
		}
		if (success) {
			if (!consumeEnergy(p, 3)) return;
			p.getLocation().getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
			p.setCooldown(tool, this.getCooldown2() * 20);
		}
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.FARMER;
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

	@Override
	public @NotNull ItemStack roleBook() {
		// [페이지 1] 대지의 생명력을 가꾸는 농부 설명
		String page1 = """
      		&0&l[ &2&l농부 가이드 &0&l ]&r
      
      		&0농부는 대지의 결실을 가꾸어
      		&0모든 이의 허기를 달래주는
      		&0&l없어서는 안 될&r&0 생산직입니다.
      
     	 	&7&m-----------------
      		&0&l[ &1&l전직 계보 &0&l ]&r
      		&0- &82차 전직: &8&o준비 중
      		&0- &83차 전직: &8&o준비 중
      		""";

		String page2 = String.format("""
      		&0&l[ &2&l보유 스킬 &0&l ]&r
      
      		&2&l▶ &0&l풍요의 손길 &8[%d초]
     	 	&0자신의 구역 내 자라난 작물을
      		&0&l일제히&r&0 수확하여 결실을 맺습니다.
      
      		&2&l▶ &0&l시간의 축복 &8[%d초]
      		&0자연의 시간을 가속하여 작물을
      		&a&l즉시 성장&r&0 단계로 이끕니다.
      
      		&7&m-----------------
      		""", getCooldown1(), getCooldown2());

		// 부모 클래스의 메서드 활용 (2페이지 구성)
		return createGuideBook("농부", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}
