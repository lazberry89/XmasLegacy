package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Warrior;

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
import org.lazberry.xmaslegacy.RoleManagers.RoleClass;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Utils.Config;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.ParseItem;
import org.lazberry.xmaslegacy.XmasLegacy;

@Roles
public class Warrior extends AbstractFirstRole implements RoleClass {
	private Material weapon_item;
	private Material armor_item;

	private final @NotNull Tomahawk tomahawk = new Tomahawk();
	private final @NotNull BloodFrenzy frenzy;

	private Container container;

	public record Container(
			XmasLegacy plugin,
			double first_skill_usable_higher_rate,
			double first_skill_usable_rate,
			int first_skill_hunger_cost,
			int first_skill_duration,
			int first_skill_strength_amplifier,
			int first_skill_strength_amplifier2,
			int first_skill_speed_amplifier,
			int second_skill_hunger_cost,
			double second_skill_damage,
			Material weapon_item
	) implements RoleContainer {}

	public Warrior() {
		super(BasicRoles.WARRIOR);
		this.loadRoleData(getRole().name().toLowerCase());
		this.frenzy = new BloodFrenzy(getCooldown1());
	}

	@Override
	protected void loadCustomStats(@NotNull FileConfiguration config) {
		Config.of(config)
				.setDefault("stats.first_skill_usable_higher_rate", 0.25)
				.setDefault("stats.first_skill_usable_rate", 0.5)
				.setDefault("stats.first_skill_hunger_cost", 3)
				.setDefault("stats.first_skill_duration", 60)
				.setDefault("stats.first_skill_strength_amplifier2", 2)
				.setDefault("stats.first_skill_strength_amplifier", 1)
				.setDefault("stats.first_skill_speed_amplifier", 1)
				.setDefault("stats.second_skill_hunger_cost", 3)
				.setDefault("stats.second_skill_damage", 6.0)
				.setDefault("tool.role_weapon", "IRON_AXE")
				.setDefault("tool.role_armor", "IRON_CHESTPLATE");

		this.weapon_item = ParseItem.parse(config.getString("tool.role_weapon"), Material.IRON_AXE);
		this.armor_item = ParseItem.parse(config.getString("tool.role_armor"), Material.IRON_CHESTPLATE);

		this.container = new Container(
				getPlugin(),
				config.getDouble("stats.first_skill_usable_higher_rate", 0.25),
				config.getDouble("stats.first_skill_usable_rate", 0.5),
				config.getInt("stats.first_skill_hunger_cost", 3),
				config.getInt("stats.first_skill_duration", 60),
				config.getInt("stats.first_skill_strength_amplifier2", 2),
				config.getInt("stats.first_skill_strength_amplifier", 1),
				config.getInt("stats.first_skill_speed_amplifier", 1),
				config.getInt("stats.second_skill_hunger_cost", 3),
				config.getDouble("stats.second_skill_damage", 6.0),
				this.weapon_item
		);
	}

	@Override
	public void useFirstSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.TARGET)) return;
		this.frenzy.execute(p, container);
	}

	@Override
	public void useSecondSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.RANGE)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		tomahawk.execute(p, container);
		p.setCooldown(tool, this.getCooldown2() * 20);
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), weapon_item)
				.setName(ColorUtils.chat("&8&l무거운 도끼"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setRoleDefault(this.getRole())
				.build().clone();
	}

    @Override
    public @NotNull ItemStack roleArmor() {
        return ItemBuilder.of(getPlugin(), armor_item)
		        .setName(ColorUtils.chat("&8&l전사의 갑옷"))
		        .setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
		        .setUnbreakable()
		        .hideAllFlags()
		        .setArmorState(9, EquipmentSlotGroup.CHEST)
                .addAttribute(Attribute.SCALE, 0.2, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ARMOR)
                .addAttribute(Attribute.MAX_HEALTH, 4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ARMOR)
		        .setRoleDefault(this.getRole())
		        .build().clone();
    }

	@Override
	public @NotNull ItemStack roleBook() {
		// [페이지 1] 야성적인 파괴력을 강조한 직업 설명
		String page1 = """
          &0&l[ &4&l전사 가이드 &0&l ]&r
          
          &0전사는 강력한 공격력과
          &0단단한 신체를 바탕으로 적진을
          &c&l분쇄&r&0하는 근접 전투의 주역입니다.
          
          &7&m-----------------
          &0&l[ &1&l전직 계보 &0&l ]&r
          &0- &82차 전직: &0버서커, 파이터
          &0- &83차 전직: &8&o추후 공개 예정
          """;

    // [페이지 2] 스킬 설명
    String page2 = String.format("""
          &0&l[ &2&l보유 스킬 &0&l ]&r
          
          &4&l▶ &0&l토마호크 &8[%d초]
          &0도끼를 던져 적중 시 대상의
          &1&l등 뒤&r&0로 즉시 이동합니다.
          
          &4&l▶ &0&l프렌지 &8[%d초]
          &0자신의 혈기를 소모하여 주변의
          &0적들을 &c&l공중&r&0으로 띄워버립니다.
          &7&m-----------------
          """, getCooldown1(), getCooldown2());
		return createGuideBook("전사", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}

}
