package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Mage;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
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
		config.addDefault("stats.first_skill_hunger_cost", 6);
		config.addDefault("stats.first_skill_speed", 0.4);
		config.addDefault("stats.first_skill_max_ticks", 60);
		config.addDefault("stats.first_skill_explosion_power", 4.0);
		config.addDefault("stats.first_skill_slow_range_x", 1.3);
		config.addDefault("stats.first_skill_slow_range_y", 2.0);
		config.addDefault("stats.first_skill_slow_range_z", 1.3);
		config.addDefault("stats.first_skill_slow_duration", 20);
		config.addDefault("stats.first_skill_slow_amplifier", 1);

		config.addDefault("stats.second_skill_hunger_cost", 8);
		config.addDefault("stats.second_skill_distance", 8.0);
		config.addDefault("stats.second_skill_max_ticks", 50);
		config.addDefault("stats.second_skill_pull_strength", 0.35);
		config.addDefault("stats.second_skill_pull_threshold", 0.6);
		config.addDefault("stats.second_skill_display_material", "PURPLE_STAINED_GLASS");

		config.addDefault("tool.role_weapon", "BREEZE_ROD");
		config.addDefault("tool.role_armor", "DIAMOND_CHESTPLATE");

		int first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 6);
		double first_skill_speed = config.getDouble("stats.first_skill_speed", 0.4);
		int first_skill_max_ticks = config.getInt("stats.first_skill_max_ticks", 60);
		double first_skill_explosion_power = config.getDouble("stats.first_skill_explosion_power", 4.0);
		double first_skill_slow_range_x = config.getDouble("stats.first_skill_slow_range_x", 1.3);
		double first_skill_slow_range_y = config.getDouble("stats.first_skill_slow_range_y", 2.0);
		double first_skill_slow_range_z = config.getDouble("stats.first_skill_slow_range_z", 1.3);
		int first_skill_slow_duration = config.getInt("stats.first_skill_slow_duration", 20);
		int first_skill_slow_amplifier = config.getInt("stats.first_skill_slow_amplifier", 1);

		int second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 8);
		double second_skill_distance = config.getDouble("stats.second_skill_distance", 8.0);
		int second_skill_max_ticks = config.getInt("stats.second_skill_max_ticks", 50);
		double second_skill_pull_strength = config.getDouble("stats.second_skill_pull_strength", 0.35);
		double second_skill_pull_threshold = config.getDouble("stats.second_skill_pull_threshold", 0.6);

		this.weapon_item = ParseItem.parse(config.getString("tool.role_weapon"), Material.BREEZE_ROD);
		this.armor_item = ParseItem.parse(config.getString("tool.role_armor"), Material.DIAMOND_CHESTPLATE);
		Material second_skill_display_material = ParseItem.parse(config.getString("stats.second_skill_display_material"), Material.PURPLE_STAINED_GLASS);

		this.container = new Container(
				getPlugin(),
				first_skill_hunger_cost,
				first_skill_speed,
				first_skill_max_ticks,
				first_skill_explosion_power,
				first_skill_slow_range_x,
				first_skill_slow_range_y,
				first_skill_slow_range_z,
				first_skill_slow_duration,
				first_skill_slow_amplifier,
				second_skill_hunger_cost,
				second_skill_distance,
				second_skill_display_material,
				second_skill_max_ticks,
				second_skill_pull_strength,
				second_skill_pull_threshold
		);
	}

	@Override
	public void useFirstSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.TARGET)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		point.execute(p, container);
		p.setCooldown(tool, getCooldown1() * 20);
	}

	@Override
	public void useSecondSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.RANGE)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		gravity.execute(p, container);
		p.setCooldown(tool, getCooldown2() * 20);
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