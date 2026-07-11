package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Rogue;

import lombok.Getter;
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
public class Rogue extends AbstractFirstRole {
	private @Getter Material weapon_item;
	private @Getter Material armor_item;

	private Container container;

	private final @NotNull DaggerRush rush = new DaggerRush();
	private final @NotNull Smoke smoke = new Smoke();

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
		var configs = Config.of(config);
		configs.setDefault("stats.first_skill_hunger_cost", 3)
				.setDefault("stats.first_skill_range", 10)
				.setDefault("stats.first_skill_speed", 2.5)
				.setDefault("stats.first_skill_y_velocity", 0.2)
				.setDefault("stats.first_skill_hit_range", 2.0)
				.setDefault("stats.first_skill_timeout_ticks", 20)
				.setDefault("stats.dagger_rush_hits", 5)
				.setDefault("stats.dagger_rush_damage", 2.0)
				.setDefault("stats.second_skill_hunger_cost", 3)
				.setDefault("stats.second_skill_duration", 100)
				.setDefault("tool.role_weapon", "IRON_SWORD")
				.setDefault("tool.role_armor", "IRON_BOOTS");

		this.weapon_item = ParseItem.parse(config.getString("tool.role_weapon"), Material.IRON_SWORD);
		this.armor_item = ParseItem.parse(config.getString("tool.role_armor"), Material.IRON_BOOTS);

		this.container = new Container(
				getPlugin(),
				configs.getValue("stats.first_skill_hunger_cost", 3),
				configs.getValue("stats.first_skill_range", 10),
				configs.getValue("stats.first_skill_speed", 2.5),
				configs.getValue("stats.first_skill_y_velocity", 0.2),
				configs.getValue("stats.first_skill_hit_range", 2.0),
				configs.getValue("stats.first_skill_timeout_ticks", 20),
				configs.getValue("stats.dagger_rush_hits", 5),
				configs.getValue("stats.dagger_rush_damage", 2.0),
				configs.getValue("stats.second_skill_hunger_cost", 3),
				configs.getValue("stats.second_skill_duration", 100L)
		);
	}

	@Override
	public void useFirstSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.TARGET, rush, container, getCooldown1());
	}

	@Override
	public void useSecondSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.RANGE, smoke, container, getCooldown2());
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