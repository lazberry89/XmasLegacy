package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Priest;

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
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Priest.Skill.CompactHeal;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Priest.Skill.Steroid;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Utils.Config;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.ParseItem;
import org.lazberry.xmaslegacy.XmasLegacy;

@Roles
public class Priest extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;

	private final @NotNull CompactHeal heal = new CompactHeal();
	private final @NotNull Steroid steroid = new Steroid();

	private Container container;

	public record Container(
			XmasLegacy plugin,
			double weapon_attack_damage,
			double armor_state_value,
			double armor_toughness_value,
			int first_skill_hunger_cost,
			double first_skill_raytrace_range,
			int first_skill_regen_duration,
			int first_skill_regen_amplifier,
			int second_skill_hunger_cost,
			int second_skill_strength_duration,
			int second_skill_strength_amplifier,
			double second_skill_radius
	) implements RoleContainer {}

	public Priest() {
		super(BasicRoles.PRIEST);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(@NotNull FileConfiguration config) {
		var configs = Config.of(config);
		configs.setDefault("stats.weapon_attack_damage", 5.0)
				.setDefault("stats.armor_state_value", 5.0)
				.setDefault("stats.armor_toughness_value", 5.0)
				.setDefault("stats.first_skill_hunger_cost", 3)
				.setDefault("stats.first_skill_raytrace_range", 15.0)
				.setDefault("stats.first_skill_regen_duration", 100)
				.setDefault("stats.first_skill_regen_amplifier", 1)
				.setDefault("stats.second_skill_hunger_cost", 3)
				.setDefault("stats.second_skill_strength_duration", 100)
				.setDefault("stats.second_skill_strength_amplifier", 1)
				.setDefault("stats.second_skill_radius", 5.0)
				.setDefault("tool.role_weapon", "GOLDEN_SPEAR")
				.setDefault("tool.role_armor", "GOLDEN_CHESTPLATE");

		this.weapon_item = ParseItem.parse(configs.getValue("tool.role_weapon"), Material.GOLDEN_SPEAR);
		this.armor_item = ParseItem.parse(configs.getValue("tool.role_armor"), Material.GOLDEN_CHESTPLATE);

		this.container = new Container(
				getPlugin(),
				configs.getValue("stats.weapon_attack_damage", 5.0),
				configs.getValue("stats.armor_state_value", 5.0),
				configs.getValue("stats.armor_toughness_value", 5.0),
				configs.getValue("stats.first_skill_hunger_cost", 3),
				configs.getValue("stats.first_skill_raytrace_range", 15.0),
				configs.getValue("stats.first_skill_regen_duration", 100),
				configs.getValue("stats.first_skill_regen_amplifier", 1),
				configs.getValue("stats.second_skill_hunger_cost", 3),
				configs.getValue("stats.second_skill_strength_duration", 100),
				configs.getValue("stats.second_skill_strength_amplifier", 1),
				configs.getValue("stats.second_skill_radius", 5.0)
		);
	}

	@Override
	public void useFirstSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.TARGET, heal, container, getCooldown1());
	}

	@Override
	public void useSecondSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.RANGE, steroid, container, getCooldown2());
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&e&l힐링 스피어"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setRoleDefault(this.getRole())
				.addAttribute(Attribute.ATTACK_DAMAGE, container.weapon_attack_damage(), AttributeModifier.Operation.ADD_NUMBER)
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&e&l단단한 근육"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setArmorState(container.armor_state_value(), EquipmentSlotGroup.CHEST)
				.addAttribute(Attribute.ARMOR_TOUGHNESS, container.armor_toughness_value(), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
				.setRoleDefault(this.getRole())
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleBook() {
		String page1 = """
           &0&l[ &e&l성직자 가이드 &0&l ]&r
      
           &0성직자는 아군을 치유하고
           &0신성한 축복을 내려 승리를
           &0이끄는 &d&l헌신적인 조력자&r&0입니다.
      
           &7&m-----------------
           &0&l[ &1&l전직 계보 &0&l ]&r
           &0- &82차 전직: &0주교, 수도사
           &0- &83차 전직: &0SAINT
           """;

		String page2 = String.format("""
           &0&l[ &2&l보유 스킬 &0&l ]&r
      
           &e&l▶ &0&l컴팩트 힐 &8[%d초]
           &0성스러운 빛으로 아군의
           &0&l생명력&r&0을 즉시 회복시킵니다.
      
           &e&l▶ &0&l스테로이드 &8[%d초]
           &0주변 동료들에게 &c&l힘의 근원&r&0을
           &0부여하여 전투력을 높여줍니다.
           &7&m-----------------
           """, getCooldown1(), getCooldown2());

		return createGuideBook("성직자", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}