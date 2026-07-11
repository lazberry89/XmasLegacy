package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Mage;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Utils.Config;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.ParseItem;
import org.lazberry.xmaslegacy.XmasLegacy;

@Roles
public class Mage extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;

	private final @NotNull CompactPoint point = new CompactPoint();
	private final @NotNull Gravity gravity = new Gravity();

	public Container container;

	public record Container(
			XmasLegacy plugin,
			int first_skill_hunger_cost,
			double first_skill_speed,
			int first_skill_max_ticks,
			double first_skill_explosion_power,
			double first_skill_slow_range_x,
			double first_skill_slow_range_y,
			double first_skill_slow_range_z,
			int first_skill_slow_duration,
			int first_skill_slow_amplifier,
			int second_skill_hunger_cost,
			double second_skill_distance,
			Material second_skill_display_material,
			int second_skill_max_ticks,
			double second_skill_pull_strength,
			double second_skill_pull_threshold
	) implements RoleContainer {}

	public Mage() {
		super(BasicRoles.MAGE);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(@NotNull FileConfiguration config) {
		var configs = Config.of(config);
		configs.setDefault("stats.first_skill_hunger_cost", 6)
				.setDefault("stats.first_skill_speed", 0.4)
				.setDefault("stats.first_skill_max_ticks", 60)
				.setDefault("stats.first_skill_explosion_power", 4.0)
				.setDefault("stats.first_skill_slow_range_x", 1.3)
				.setDefault("stats.first_skill_slow_range_y", 2.0)
				.setDefault("stats.first_skill_slow_range_z", 1.3)
				.setDefault("stats.first_skill_slow_duration", 20)
				.setDefault("stats.first_skill_slow_amplifier", 1)
				.setDefault("stats.second_skill_hunger_cost", 8)
				.setDefault("stats.second_skill_distance", 8.0)
				.setDefault("stats.second_skill_max_ticks", 50)
				.setDefault("stats.second_skill_pull_strength", 0.35)
				.setDefault("stats.second_skill_pull_threshold", 0.6)
				.setDefault("stats.second_skill_display_material", "PURPLE_STAINED_GLASS")
				.setDefault("tool.role_weapon", "BREEZE_ROD")
				.setDefault("tool.role_armor", "DIAMOND_CHESTPLATE");

		this.weapon_item = ParseItem.parse(configs.getValue("tool.role_weapon"), Material.BREEZE_ROD);
		this.armor_item = ParseItem.parse(configs.getValue("tool.role_armor"), Material.DIAMOND_CHESTPLATE);

		this.container = new Container(
				getPlugin(),
				configs.getValue("stats.first_skill_hunger_cost", 6),
				configs.getValue("stats.first_skill_speed", 0.4),
				configs.getValue("stats.first_skill_max_ticks", 60),
				configs.getValue("stats.first_skill_explosion_power", 4.0),
				configs.getValue("stats.first_skill_slow_range_x", 1.3),
				configs.getValue("stats.first_skill_slow_range_y", 2.0),
				configs.getValue("stats.first_skill_slow_range_z", 1.3),
				configs.getValue("stats.first_skill_slow_duration", 20),
				configs.getValue("stats.first_skill_slow_amplifier", 1),
				configs.getValue("stats.second_skill_hunger_cost", 8),
				configs.getValue("stats.second_skill_distance", 8.0),
				ParseItem.parse(configs.getValue("stats.second_skill_display_material"), Material.PURPLE_STAINED_GLASS),
				configs.getValue("stats.second_skill_max_ticks", 50),
				configs.getValue("stats.second_skill_pull_strength", 0.35),
				configs.getValue("stats.second_skill_pull_threshold", 0.6)
		);
	}

	@Override
	public void useFirstSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.TARGET, point, container, getCooldown1());
	}

	@Override
	public void useSecondSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.RANGE, gravity, container, getCooldown2());
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&7&l일반 지팡이"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setAttackDamage(4)
				.setRoleDefault(this.getRole())
				.hideAllFlags()
				.addAttribute(Attribute.MOVEMENT_SPEED, -0.08, AttributeModifier.Operation.ADD_NUMBER)
				.setGlint(true)
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&7&l보호구"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.addAttribute(Attribute.JUMP_STRENGTH, 0.04, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
				.setArmorState(7, EquipmentSlotGroup.CHEST)
				.setRoleDefault(this.getRole())
				.build().clone();
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
		String page1 = """
           &0&l[ &5&l마법사 가이드 &0&l ]&r
      
           &0마법사는 원소의 힘을 빌려
           &0범위 내의 모든 적을 궤멸시키는
           &5&l강력한 마력&r&0의 소유자입니다.
      
           &7&m-----------------
           &0&l[ &1&l전직 계보 &0&l ]&r
           &0- &82차 전직: &0위자드, 엘리멘탈, 소환사
           &0- &83차 전직: &0아크메이지
           """;

		String page2 = String.format("""
           &0&l[ &2&l보유 스킬 &0&l ]&r
      
           &5&l▶ &0&l극점 &8[%d초]
           &0느리지만 파괴적인 &c&l중력구&r&0를
           &0사출하여 경로상의 적을 압살합니다.
      
           &5&l▶ &0&l중력장 &8[%d초]
           &0공간을 왜곡하여 주변의 적을
           &0&l중심점&r&0으로 강하게 끌어당깁니다.
           &7&m-----------------
           """, getCooldown1(), getCooldown2());

		return createGuideBook("마법사", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}