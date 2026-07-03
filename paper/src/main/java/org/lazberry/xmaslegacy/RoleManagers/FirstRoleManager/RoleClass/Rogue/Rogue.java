package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Rogue;

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
public class Rogue extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;

	private Container container;

	private final DaggerRush rush = new DaggerRush();
	private final Smoke smoke = new Smoke();

	public record Container(
		XmasLegacy plugin,
		int first_skill_hunger_cost,
		int first_skill_range,
		double first_skill_speed,
		double first_skill_y_velocity,
		double first_skill_hit_range,
		int first_skill_timeout_ticks,
		int dagger_rush_hits,
		double dagger_rush_damage,
		int second_skill_hunger_cost,
		long second_skill_duration
	) implements RoleContainer {}

	public Rogue() {
		super(BasicRoles.ROGUE);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(@NotNull FileConfiguration config) {
		config.addDefault("stats.first_skill_hunger_cost", 3);
		config.addDefault("stats.first_skill_range", 10);
		config.addDefault("stats.first_skill_speed", 2.5);
		config.addDefault("stats.first_skill_y_velocity", 0.2);
		config.addDefault("stats.first_skill_hit_range", 2.0);
		config.addDefault("stats.first_skill_timeout_ticks", 20);
		config.addDefault("stats.dagger_rush_hits", 5);
		config.addDefault("stats.dagger_rush_damage", 2.0);
		config.addDefault("stats.second_skill_hunger_cost", 3);
		config.addDefault("stats.second_skill_duration", 100);

		config.addDefault("tool.role_weapon", "IRON_SWORD");
		config.addDefault("tool.role_armor", "IRON_BOOTS");

		int first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		int first_skill_range = config.getInt("stats.first_skill_range", 10);
		double first_skill_speed = config.getDouble("stats.first_skill_speed", 2.5);
		double first_skill_y_velocity = config.getDouble("stats.first_skill_y_velocity", 0.2);
		double first_skill_hit_range = config.getDouble("stats.first_skill_hit_range", 2.0);
		int first_skill_timeout_ticks = config.getInt("stats.first_skill_timeout_ticks", 20);
		int dagger_rush_hits = config.getInt("stats.dagger_rush_hits", 5);
		double dagger_rush_damage = config.getDouble("stats.dagger_rush_damage", 2.0);
		int second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 3);
		long second_skill_duration = config.getLong("stats.second_skill_duration", 100L);

		this.weapon_item = ParseItem.parse(config.getString("tool.role_weapon"), Material.IRON_SWORD);
		this.armor_item = ParseItem.parse(config.getString("tool.role_armor"), Material.IRON_BOOTS);

		this.container = new Container(
			getPlugin(),
				first_skill_hunger_cost,
				first_skill_range,
				first_skill_speed,
				first_skill_y_velocity,
				first_skill_hit_range,
				first_skill_timeout_ticks,
				dagger_rush_hits,
				dagger_rush_damage,
				second_skill_hunger_cost,
				second_skill_duration
		);
	}

	@Override
	public void useFirstSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.TARGET)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		rush.execute(p, container);
		p.setCooldown(tool, this.getCooldown1() * 20);
	}

	@Override
	public void useSecondSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.RANGE)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		smoke.execute(p, container);
		p.setCooldown(tool, this.getCooldown2() * 20);
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&7&l무딘 단검"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				//.setItemModel("knife")
				.setRoleDefault(this.getRole())
				.setAttackDamage(5.0)
				.addAttribute(Attribute.MOVEMENT_SPEED, 0.02, AttributeModifier.Operation.ADD_NUMBER)
				.addAttribute(Attribute.ATTACK_SPEED, 0.02, AttributeModifier.Operation.ADD_NUMBER)
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&7&l낡은 부츠"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setItemModel("RogueArmor")
				.setRoleDefault(this.getRole())
				.setArmorState(5.0, EquipmentSlotGroup.FEET)
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleBook() {
		String page1 = """
          &0&l[ &8&l도적 가이드 &0&l ]&r
          
          &0도적은 그림자 속에 숨어들어
          &0적의 빈틈을 노리는 &b&l민첩함&r&0과
          &0기술이 핵심인 암살자입니다.
          
          &7&m-----------------
          &0&l[ &1&l전직 계보 &0&l ]&r
          &0- &82차 전직: &0어쌔신, 리퍼
          &0- &83차 전직: &0Mr.Shadow
          """;

		String page2 = String.format("""
          &0&l[ &2&l보유 스킬 &0&l ]&r

          &8&l▶ &0&l돌진기 &8[%d초]
          &0표적을 포착하여 순식간에
          &0&l5연속&r&0으로 급소를 타격합니다.
          
          &8&l▶ &0&l연막탄 &8[%d초]
          &0연막 속으로 자취를 감추고
          &b&l이동 속도&r&0가 크게 증가합니다.
          &7&m-----------------
          """, getCooldown1(), getCooldown2());

		return createGuideBook("도적", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}