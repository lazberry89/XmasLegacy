package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Gatherer;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
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
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Utils.Config;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.ParseItem;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.BasicSkills;

@Roles
public class Gatherer extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;

	private Container container;

	public record Container(
			XmasLegacy plugin,
			double weapon_movement_speed,
			double armor_movement_speed,
			int first_skill_hunger_cost,
			int first_skill_target_range,
			int first_skill_particle_count,
			double first_skill_particle_offset,
			double first_skill_particle_speed,
			float first_skill_particle_size,
			int second_skill_hunger_cost,
			int second_skill_entity_range,
			int second_skill_container_range,
			long second_skill_glow_duration
	) implements RoleContainer {}

	public Gatherer() {
		super(BasicRoles.GATHERER);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(@NotNull FileConfiguration config) {
		var configs = Config.of(config);
		configs.setDefault("stats.weapon_movement_speed", 0.01)
				.setDefault("stats.armor_movement_speed", 0.01)
				.setDefault("stats.first_skill_hunger_cost", 3)
				.setDefault("stats.first_skill_target_range", 7)
				.setDefault("stats.first_skill_particle_count", 15)
				.setDefault("stats.first_skill_particle_offset", 0.5)
				.setDefault("stats.first_skill_particle_speed", 0.01)
				.setDefault("stats.first_skill_particle_size", 1.5)
				.setDefault("stats.second_skill_hunger_cost", 8)
				.setDefault("stats.second_skill_entity_range", 12)
				.setDefault("stats.second_skill_container_range", 8)
				.setDefault("stats.second_skill_glow_duration", 40L)
				.setDefault("tool.role_weapon", "COMPASS")
				.setDefault("tool.role_armor", "GOLDEN_BOOTS");

		this.weapon_item = ParseItem.parse(configs.getValue("tool.role_weapon"), Material.COMPASS);
		this.armor_item = ParseItem.parse(configs.getValue("tool.role_armor"), Material.GOLDEN_BOOTS);

		this.container = new Container(
				getPlugin(),
				configs.getValue("stats.weapon_movement_speed", 0.01),
				configs.getValue("stats.armor_movement_speed", 0.01),
				configs.getValue("stats.first_skill_hunger_cost", 3),
				configs.getValue("stats.first_skill_target_range", 7),
				configs.getValue("stats.first_skill_particle_count", 15),
				configs.getValue("stats.first_skill_particle_offset", 0.5),
				configs.getValue("stats.first_skill_particle_speed", 0.01),
				configs.getValue("stats.first_skill_particle_size", 1.5f),
				configs.getValue("stats.second_skill_hunger_cost", 8),
				configs.getValue("stats.second_skill_entity_range", 12),
				configs.getValue("stats.second_skill_container_range", 8),
				configs.getValue("stats.second_skill_glow_duration", 40L)
		);
	}

	@Override
	public void useFirstSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.TARGET, getSkillRepo().get(BasicSkills.ETERNAL_POSE), container, getCooldown1());
	}

	@Override
	public void useSecondSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.RANGE, getSkillRepo().get(BasicSkills.TRUTH_EYE), container, getCooldown2());
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&6&l최후의 길잡이"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setRoleDefault(this.getRole())
				.hideAllFlags()
				.addAttribute(Attribute.MOVEMENT_SPEED, container.weapon_movement_speed(), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&6&l길잡이의 유물"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setRoleDefault(this.getRole())
				.hideAllFlags()
				.setUnbreakable()
				.addAttribute(Attribute.MOVEMENT_SPEED, container.armor_movement_speed(), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET)
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleBook() {
		String page1 = """
      		&0&l[ &6&l수집가 가이드 &0&l ]&r
      
      		&0수집가는 금지된 유적을 탐사하며
      		&0세상에 알려지지 않은 &3&l비밀&r&0과
      		&0유물을 찾아내는 탐험가입니다.
      
      		&7&m-----------------
      		&0&l[ &1&l전직 계보 &0&l ]&r
      		&0- &82차 전직: &8&o준비 중
      		&0- &83차 전직: &8&o준비 중
      		""";

		String page2 = String.format("""
      		&0&l[ &2&l보유 스킬 &0&l ]&r
      
      		&6&l▶ &0&l회귀의 바늘 &8[%d초]
      		&0공간의 좌표를 &1&l이터널포스&r&0로
      		&0고정하여 회귀 지점을 설정합니다.
      
      		&6&l▶ &0&l에테르의 눈 &8[%d초]
      		&0에테르와 공명하여 주변의 적과
      		&d&l루트 상자&r&0를 꿰뚫어 봅니다.
      		&7&m-----------------
      		""", getCooldown1(), getCooldown2());

		return createGuideBook("수집가", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}