package xmaslegacy.RoleManagers.FirstRoleManager.Farmer;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import xmaslegacy.Emblems.EmblemType;
import xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import xmaslegacy.PlayerSkillUseEvent;
import xmaslegacy.Region.Region;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.Utils.ItemBuilder;

import java.util.*;

@SuppressWarnings("DuplicatedCode")
@xmaslegacy.Annotation.Roles
public class Farmer extends AbstractFirstRole {
	private final RegionManager rm;
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.RADIUS_HARVEST);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}
	private Material weapon_item;
	private Material armor_item;
	private double armor_state_value;
	private int first_skill_hunger_cost;
	private int first_skill_radius;
	private int second_skill_hunger_cost;
	private int second_skill_radius;
	private int second_skill_y_range;
	private int second_skill_particle_count;
	private double second_skill_particle_offset;

	public Farmer() {
		super(Roles.FARMER);
		this.rm = RegionManager.INSTANCE;
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
		config.addDefault("stats.armor_state_value", 5.0);

		config.addDefault("stats.first_skill_hunger_cost", 3);
		config.addDefault("stats.first_skill_radius", 4);

		config.addDefault("stats.second_skill_hunger_cost", 3);
		config.addDefault("stats.second_skill_radius", 4);
		config.addDefault("stats.second_skill_y_range", 2);
		config.addDefault("stats.second_skill_particle_count", 5);
		config.addDefault("stats.second_skill_particle_offset", 0.2);

		config.addDefault("tool.role_weapon", "IRON_HOE");
		config.addDefault("tool.role_armor", "LEATHER_CHESTPLATE");

		this.armor_state_value = config.getDouble("stats.armor_state_value", 5.0);

		this.first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		this.first_skill_radius = config.getInt("stats.first_skill_radius", 4);

		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 3);
		this.second_skill_radius = config.getInt("stats.second_skill_radius", 4);
		this.second_skill_y_range = config.getInt("stats.second_skill_y_range", 2);
		this.second_skill_particle_count = config.getInt("stats.second_skill_particle_count", 5);
		this.second_skill_particle_offset = config.getDouble("stats.second_skill_particle_offset", 0.2);

		Material weapon;
		try {
			weapon = Material.valueOf(config.getString("tool.role_weapon"));
		} catch (IllegalArgumentException e) {
			weapon = Material.IRON_HOE;
		}
		this.weapon_item = weapon;

		Material armor;
		try {
			armor = Material.valueOf(config.getString("tool.role_armor"));
		} catch (IllegalArgumentException e) {
			armor = Material.LEATHER_CHESTPLATE;
		}
		this.armor_item = armor;
	}

	@Override
	public void useFirstSkill(Player p) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.TARGET);
		Bukkit.getPluginManager().callEvent(skillUse);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool.getType()) / 20 + "초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;
		List<Region> playerRegions = rm.getRegion(p);
		if (playerRegions.isEmpty()) return;

		List<Block> crops = getFullyGrownCrops(p, this.first_skill_radius);
		for (Block block : crops) {
			Region cropRegion = rm.getRegionAt(block.getLocation());

			if (cropRegion != null && playerRegions.contains(cropRegion)) {
				block.breakNaturally();
				block.getLocation().getWorld().playSound(block.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
			} else {
				p.sendMessage(ColorUtils.chat(Alert.RED + " 적절한 사용 조건이 아닙니다."));
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
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.RANGE);
		Bukkit.getPluginManager().callEvent(skillUse);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;

		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool.getType()) / 20 + "초 기다리세요"));
			return;
		}
		List<Region> playerRegions = rm.getRegion(p);
		if (playerRegions.isEmpty()) return;

		Location center = p.getLocation();
		boolean success = false;

		for (int x = -this.second_skill_radius; x <= this.second_skill_radius; x++) {
			for (int y = -this.second_skill_y_range; y <= this.second_skill_y_range; y++) {
				for (int z = -this.second_skill_radius; z <= this.second_skill_radius; z++) {
					Block block = center.clone().add(x, y, z).getBlock();

					if (block.getBlockData() instanceof Ageable ageable) {
						Region cropRegion = rm.getRegionAt(block.getLocation());

						if (cropRegion != null && playerRegions.contains(cropRegion) && ageable.getAge() < ageable.getMaximumAge()) {
							ageable.setAge(ageable.getMaximumAge());
							block.setBlockData(ageable);
							block.getWorld().spawnParticle(
									Particle.HAPPY_VILLAGER,
									block.getLocation().add(0.5, 0.5, 0.5),
									this.second_skill_particle_count,
									this.second_skill_particle_offset,
									this.second_skill_particle_offset,
									this.second_skill_particle_offset
							);
							success = true;
						} else {
							p.sendMessage(ColorUtils.chat(Alert.RED + " 적절한 사용 조건이 아닙니다."));
							p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
							return;
						}
					}
				}
			}
		}
		if (success) {
			if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;
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
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&e&l눙부의 낫"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setRoleDefault(this.getRole())
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&e&l조끼"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setArmorState(this.armor_state_value, EquipmentSlotGroup.CHEST)
				.setRoleDefault(this.getRole())
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack TargetEmblem() {
		return getEmblem().getTargetEmblem();
	}

	@Override
	public @NotNull ItemStack RangeEmblem() {
		return getEmblem().getRangeEmblem();
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
