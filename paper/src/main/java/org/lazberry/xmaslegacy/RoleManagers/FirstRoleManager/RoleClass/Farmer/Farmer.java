package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Farmer;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.SkillManager;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Utils.Config;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.ParseItem;
import org.lazberry.xmaslegacy.settings.BasicSkills;

@Roles
public class Farmer extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;

	private Container container;

	public record Container(
			double armor_state_value,
			int first_skill_hunger_cost,
			int first_skill_radius,
			int second_skill_hunger_cost,
			int second_skill_radius,
			int second_skill_y_range,
			int second_skill_particle_count,
			double second_skill_particle_offset
	) implements RoleContainer {}

	public Farmer() {
		super(BasicRoles.FARMER);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(@NotNull FileConfiguration config) {
		var configs = Config.of(config);
		configs.setDefault("stats.armor_state_value", 5.0)
				.setDefault("stats.first_skill_hunger_cost", 3)
				.setDefault("stats.first_skill_radius", 4)
				.setDefault("stats.second_skill_hunger_cost", 3)
				.setDefault("stats.second_skill_radius", 4)
				.setDefault("stats.second_skill_y_range", 2)
				.setDefault("stats.second_skill_particle_count", 5)
				.setDefault("stats.second_skill_particle_offset", 0.2)
				.setDefault("tool.role_weapon", "IRON_HOE")
				.setDefault("tool.role_armor", "LEATHER_CHESTPLATE");

		this.weapon_item = ParseItem.parse(configs.getValue("tool.role_weapon"), Material.IRON_HOE);
		this.armor_item = ParseItem.parse(configs.getValue("tool.role_armor"), Material.LEATHER_CHESTPLATE);

		this.container = new Container(
				configs.getValue("stats.armor_state_value", 5.0),
				configs.getValue("stats.first_skill_hunger_cost", 3),
				configs.getValue("stats.first_skill_radius", 4),
				configs.getValue("stats.second_skill_hunger_cost", 3),
				configs.getValue("stats.second_skill_radius", 4),
				configs.getValue("stats.second_skill_y_range", 2),
				configs.getValue("stats.second_skill_particle_count", 5),
				configs.getValue("stats.second_skill_particle_offset", 0.2)
		);
	}

	@Override
	public void useFirstSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.TARGET, SkillManager.INSTANCE.get(BasicSkills.RADIUS_HARVEST), container, getCooldown1());
	}

	@Override
	public void useSecondSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.RANGE, SkillManager.INSTANCE.get(BasicSkills.SPEED_GROWER), container, getCooldown2());
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
				.setArmorState(container.armor_state_value(), EquipmentSlotGroup.CHEST)
				.setRoleDefault(this.getRole())
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
