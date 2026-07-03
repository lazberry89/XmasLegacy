package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Crafter;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Roles;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;

@Roles
public class Crafter extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;
	private int first_skill_raytrace_range;
	private int first_skill_hunger_cost;
	private double first_skill_repair_percent;
	private int first_skill_cooldown_ticks;
	private int second_skill_raytrace_range;
	private double second_skill_mining_efficiency_buff;
	private double second_skill_attack_damage_buff;
	private int second_skill_hunger_cost;
	private int second_skill_cooldown_ticks;

	private Container container;

	private final @NotNull Skills<Crafter.Container> fix = new Fix();
	private final @NotNull Skills<Crafter.Container> tempBuff = new TempBuff();

	public Crafter() {
		super(BasicRoles.CRAFTER);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	public record Container(
		ItemStack item,
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
		// 1. 장인 전용 YAML 스탯 기본값 주입
		config.addDefault("stats.first_skill_raytrace_range", 5);
		config.addDefault("stats.first_skill_hunger_cost", 3);
		config.addDefault("stats.first_skill_repair_percent", 0.21);
		config.addDefault("stats.first_skill_cooldown_ticks", 100);

		config.addDefault("stats.second_skill_raytrace_range", 5);
		config.addDefault("stats.second_skill_mining_efficiency_buff", 2.0);
		config.addDefault("stats.second_skill_attack_damage_buff", 2.0);
		config.addDefault("stats.second_skill_hunger_cost", 5);
		config.addDefault("stats.second_skill_cooldown_ticks", 200);

		config.addDefault("tool.role_weapon", "ANVIL");
		config.addDefault("tool.role_armor", "IRON_CHESTPLATE");

		// 2. 파일 변수 바인딩 수립
		this.first_skill_raytrace_range = config.getInt("stats.first_skill_raytrace_range", 5);
		this.first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		this.first_skill_repair_percent = config.getDouble("stats.first_skill_repair_percent", 0.21);
		this.first_skill_cooldown_ticks = config.getInt("stats.first_skill_cooldown_ticks", 100);

		this.second_skill_raytrace_range = config.getInt("stats.second_skill_raytrace_range", 5);
		this.second_skill_mining_efficiency_buff = config.getDouble("stats.second_skill_mining_efficiency_buff", 2.0);
		this.second_skill_attack_damage_buff = config.getDouble("stats.second_skill_attack_damage_buff", 2.0);
		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 5);
		this.second_skill_cooldown_ticks = config.getInt("stats.second_skill_cooldown_ticks", 200);

		Material weapon;
		try {
			weapon = Material.valueOf(config.getString("tool.role_weapon"));
		} catch (IllegalArgumentException e) {
			weapon = Material.ANVIL;
		}
		this.weapon_item = weapon;

		Material armor;
		try {
			armor = Material.valueOf(config.getString("tool.role_armor"));
		} catch (IllegalArgumentException e) {
			armor = Material.IRON_CHESTPLATE;
		}
		this.armor_item = armor;
	}

	@Override
	public void useFirstSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.TARGET)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();

		this.container = new Container(
				tool,
				first_skill_raytrace_range,
				first_skill_hunger_cost,
				first_skill_repair_percent,
				first_skill_cooldown_ticks,
				second_skill_raytrace_range,
				second_skill_mining_efficiency_buff,
				second_skill_attack_damage_buff,
				second_skill_hunger_cost,
				second_skill_cooldown_ticks
		);
		fix.execute(p, container);
		p.setCooldown(tool, this.first_skill_cooldown_ticks);
	}

	@Override
	public void useSecondSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.RANGE)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		this.container = new Container(
				tool,
				first_skill_raytrace_range,
				first_skill_hunger_cost,
				first_skill_repair_percent,
				first_skill_cooldown_ticks,
				second_skill_raytrace_range,
				second_skill_mining_efficiency_buff,
				second_skill_attack_damage_buff,
				second_skill_hunger_cost,
				second_skill_cooldown_ticks
		);
		tempBuff.execute(p, container);
		p.setCooldown(tool.getType(), this.second_skill_cooldown_ticks);
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

		// 부모 클래스의 메서드 활용 (2페이지 구성)
		return createGuideBook("장인", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}