package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Knight;

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

@Roles
public class Knight extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;
	private float damage;
	private double armor_state_value;

	private Container container;
	private final @NotNull SharpSweeping sharpSweeping = new SharpSweeping();
	private final @NotNull Taunt taunt = new Taunt();

	public Knight() {
		super(BasicRoles.KNIGHT);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	public record Container(
			double armor_state_value,
			int first_skill_hunger_cost,
			double first_skill_speed,
			double first_skill_y_velocity,
			int first_skill_max_ticks,
			double first_skill_range,
			double first_skill_tick_y_add,
			double first_skill_damage,
			int first_skill_slow_duration,
			int first_skill_slow_amplifier,
			double first_skill_knockback_multiplier,
			double first_skill_knockback_y,
			int second_skill_hunger_cost,
			double second_skill_range,
			long second_skill_duration,
			double second_skill_knockback,
			double second_skill_knockback_y,
			long second_skill_ai_restore_delay
	) implements RoleContainer {}

	@Override
	protected void loadCustomStats(@NotNull FileConfiguration config) {
		config.addDefault("stats.weapon_damage", 5.0);
		config.addDefault("stats.armor_state_value", 7.0);

		config.addDefault("stats.first_skill_hunger_cost", 3);
		config.addDefault("stats.first_skill_speed", 1.5);
		config.addDefault("stats.first_skill_y_velocity", 0.2);
		config.addDefault("stats.first_skill_max_ticks", 10);
		config.addDefault("stats.first_skill_range", 1.5);
		config.addDefault("stats.first_skill_tick_y_add", 0.05);
		config.addDefault("stats.first_skill_damage", 5.0);
		config.addDefault("stats.first_skill_slow_duration", 20);
		config.addDefault("stats.first_skill_slow_amplifier", 2);
		config.addDefault("stats.first_skill_knockback_multiplier", 0.5);
		config.addDefault("stats.first_skill_knockback_y", 0.2);

		config.addDefault("stats.second_skill_hunger_cost", 3);
		config.addDefault("stats.second_skill_range", 10.0);
		config.addDefault("stats.second_skill_duration", 100L);
		config.addDefault("stats.second_skill_knockback", -1.5);
		config.addDefault("stats.second_skill_knockback_y", 0.15);
		config.addDefault("stats.second_skill_ai_restore_delay", 3L);

		config.addDefault("tool.role_weapon", "IRON_SWORD");
		config.addDefault("tool.role_armor", "IRON_CHESTPLATE");

		this.damage = (float) config.getDouble("stats.weapon_damage", 5.0);
		this.armor_state_value = config.getDouble("stats.armor_state_value", 7.0);

		int first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		double first_skill_speed = config.getDouble("stats.first_skill_speed", 1.5);
		double first_skill_y_velocity = config.getDouble("stats.first_skill_y_velocity", 0.2);
		int first_skill_max_ticks = config.getInt("stats.first_skill_max_ticks", 10);
		double first_skill_range = config.getDouble("stats.first_skill_range", 1.5);
		double first_skill_tick_y_add = config.getDouble("stats.first_skill_tick_y_add", 0.05);
		double first_skill_damage = config.getDouble("stats.first_skill_damage", 5.0);
		int first_skill_slow_duration = config.getInt("stats.first_skill_slow_duration", 20);
		int first_skill_slow_amplifier = config.getInt("stats.first_skill_slow_amplifier", 2);
		double first_skill_knockback_multiplier = config.getDouble("stats.first_skill_knockback_multiplier", 0.5);
		double first_skill_knockback_y = config.getDouble("stats.first_skill_knockback_y", 0.2);

		int second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 3);
		double second_skill_range = config.getDouble("stats.second_skill_range", 10.0);
		long second_skill_duration = config.getLong("stats.second_skill_duration", 100L);
		double second_skill_knockback = config.getDouble("stats.second_skill_knockback", -1.5);
		double second_skill_knockback_y = config.getDouble("stats.second_skill_knockback_y", 0.15);
		long second_skill_ai_restore_delay = config.getLong("stats.second_skill_ai_restore_delay", 3L);

		Material weapon;
		try {
			weapon = Material.valueOf(config.getString("tool.role_weapon"));
		} catch (IllegalArgumentException e) {
			weapon = Material.IRON_SWORD;
		}
		this.weapon_item = weapon;

		Material armor;
		try {
			armor = Material.valueOf(config.getString("tool.role_armor"));
		} catch (IllegalArgumentException e) {
			armor = Material.IRON_CHESTPLATE;
		}
		this.armor_item = armor;

		this.container = new Container(
				armor_state_value,
				first_skill_hunger_cost,
				first_skill_speed,
				first_skill_y_velocity,
				first_skill_max_ticks,
				first_skill_range,
				first_skill_tick_y_add,
				first_skill_damage,
				first_skill_slow_duration,
				first_skill_slow_amplifier,
				first_skill_knockback_multiplier,
				first_skill_knockback_y,
				second_skill_hunger_cost,
				second_skill_range,
				second_skill_duration,
				second_skill_knockback,
				second_skill_knockback_y,
				second_skill_ai_restore_delay
		);
	}

	@Override
	public void useFirstSkill(Player player) {
		if (isSkillCancelled(player, this , emblem, EmblemType.TARGET)) return;
		ItemStack tool = player.getInventory().getItemInMainHand();
		sharpSweeping.execute(player, container);
		player.setCooldown(tool, this.getCooldown1() * 20);
	}

	@Override
	public void useSecondSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.RANGE)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		taunt.execute(p, container);
		p.setCooldown(tool, this.getCooldown2() * 20);
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&7&l녹슨 철검"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setItemModel("BasicSword")
				.setAttackDamage(this.damage)
				.setRoleDefault(this.getRole())
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&7&l낡은 흉갑"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setItemModel("KnightArmor")
				.setRoleDefault(this.getRole())
				.addAttribute(Attribute.ARMOR, this.armor_state_value, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
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
		String page1 = """
           &0&l[ &1&l기사 가이드 &0&l ]&r
      
           &0기사는 굳건한 방어력과 검술로
           &0동료를 보호하며 전선의 중심을
           &0지키는 &1&l명예로운 방패&r&0입니다.
      
           &7&m-----------------
           &0&l[ &1&l전직 계보 &0&l ]&r
           &0- &82차 전직: &0가디언, 디펜더
           &0- &83차 전직: &0팔라딘
           """;

		String page2 = String.format("""
           &0&l[ &2&l보유 스킬 &0&l ]&r
      
           &1&l▶ &0&l칼날돌진 &8[%d초]
           &0날카로운 기세로 전방을 향해
           &0&l연속 베기&r&0를 하며 돌진합니다.
      
           &1&l▶ &0&l광역 도발 &8[%d초]
           &0함성을 내질러 주변 적들의
           &0&l시선&r&0을 자신에게 고정시킵니다.
           &7&m-----------------
           """, getCooldown1(), getCooldown2());

		return createGuideBook("기사", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}