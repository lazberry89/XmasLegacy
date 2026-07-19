package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Crafter;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Utils.Config;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.ParseItem;

@Roles
public class Crafter extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;

	private Container container;

	private final @NotNull Skills<Crafter.Container> fix = new Fix();
	private final @NotNull Skills<Crafter.Container> tempBuff = new TempBuff();

	public Crafter() {
		super(BasicRoles.CRAFTER);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	public record Container(
		int first_skill_raytrace_range,
		int first_skill_hunger_cost,
		double first_skill_repair_percent,
		int first_skill_cooldown_ticks,
		int second_skill_raytrace_range,
		double second_skill_mining_efficiency_buff,
		double second_skill_attack_damage_buff,
		int second_skill_hunger_cost,
		int second_skill_cooldown_ticks
	) implements RoleContainer {}

	@Override
	protected void loadCustomStats(@NotNull FileConfiguration config) {
		var configs = Config.of(config);
		configs.setDefault("stats.first_skill_raytrace_range", 5)
				.setDefault("stats.first_skill_hunger_cost", 3)
				.setDefault("stats.first_skill_repair_percent", 0.21)
				.setDefault("stats.first_skill_cooldown_ticks", 100)
				.setDefault("stats.second_skill_raytrace_range", 5)
				.setDefault("stats.second_skill_mining_efficiency_buff", 2.0)
				.setDefault("stats.second_skill_attack_damage_buff", 2.0)
				.setDefault("stats.second_skill_hunger_cost", 5)
				.setDefault("stats.second_skill_cooldown_ticks", 200)
				.setDefault("tool.role_weapon", "ANVIL")
				.setDefault("tool.role_armor", "IRON_CHESTPLATE");

		this.container = new Container(
				configs.getValue("stats.first_skill_raytrace_range", 5),
				configs.getValue("stats.first_skill_hunger_cost", 3),
				configs.getValue("stats.first_skill_repair_percent", 0.21),
				configs.getValue("stats.first_skill_cooldown_ticks", 100),
				configs.getValue("stats.second_skill_raytrace_range", 5),
				configs.getValue("stats.second_skill_mining_efficiency_buff", 2.0),
				configs.getValue("stats.second_skill_attack_damage_buff", 2.0),
				configs.getValue("stats.second_skill_hunger_cost", 5),
				configs.getValue("stats.second_skill_cooldown_ticks", 200)
		);
		this.weapon_item = ParseItem.parse(configs.getValue("tool.role_weapon"), Material.ANVIL);
		this.armor_item = ParseItem.parse(configs.getValue("tool.role_armor"), Material.IRON_CHESTPLATE);
	}

	@Override
	public void useFirstSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.TARGET, fix, container, getCooldown1());
	}

	@Override
	public void useSecondSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.RANGE, tempBuff, container, getCooldown2());
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&e&l장인의 손길"))
				.setLore(ColorUtils.chat("&7장인의 땀과 기술이 담긴 장비입니다."))
				.hideAllFlags()
				.setRoleDefault(this.getRole())
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&7&l위험한 작업으로부터 장인을 보호해줍니다."))
				.setLore(ColorUtils.chat("&7장인의 경험과 기술이 담긴 갑옷입니다."))
				.hideAllFlags()
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
		String page1 = """
          &0&l[ &8&l장인 가이드 &0&l ]&r
          
          &0장인은 뛰어난 대장 기술과
          &0장비 가공 능력을 바탕으로 동료의
          &0무기를 &6&l강화&r&0하고 장비를 수리하는
          &0서포트형 전문가입니다.
          
          &7&m-----------------
          &0&l[ &1&l전직 계보 &0&l ]&r
          &0- &82차 전직: &0대장장이, 연금술사
          &0- &83차 전직: &8&o준비 중
          """;

		String page2 = String.format("""
          &0&l[ &2&l보유 스킬 &0&l ]&r

          &8&l▶ &0&l수리하기 &8[%d초]
          &0고객의 장비, 무기 등의 손상된 내구도를
          &0일정량 복구해준다.
          
          &8&l▶ &0&l일시버프 &8[%d초]
          &0도구, 무기에 맞게 한번에 한하여
          &0일정 버프가 붙는다.
          &7&m-----------------
          """, getCooldown1(), getCooldown2());
		return createGuideBook("장인", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}