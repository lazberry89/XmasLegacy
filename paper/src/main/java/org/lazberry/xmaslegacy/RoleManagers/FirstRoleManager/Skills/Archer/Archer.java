package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.Skills.Archer;

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
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;

@Roles
public class Archer extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;
	private int first_skill_hunger_cost;
	private int second_skill_hunger_cost;
	private Container container;

	private final @NotNull ShockDart shockDart = new ShockDart();
	private final @NotNull BackDash backDash = new BackDash();

	public Archer() {
		super(BasicRoles.ARCHER);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	public record Container(
			int first_skill_hunger_cost,
			double first_skill_arrow_speed,
			int first_skill_arrow_timeout,
			int second_skill_hunger_cost,
			float second_skill_explosion_power,
			double second_skill_backdash_multiplier,
			double second_skill_backdash_y,
			long second_skill_invulnerable_duration,
			XmasLegacy plugin
	) implements RoleContainer {}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
		config.addDefault("stats.first_skill_hunger_cost", 3);
		config.addDefault("stats.first_skill_arrow_speed", 2.5);
		config.addDefault("stats.first_skill_arrow_timeout", 1200); // 60 * 20
		config.addDefault("stats.second_skill_hunger_cost", 4);
		config.addDefault("stats.second_skill_explosion_power", 2.0);
		config.addDefault("stats.second_skill_backdash_multiplier", -2.0);
		config.addDefault("stats.second_skill_backdash_y", 0.3);
		config.addDefault("stats.second_skill_invulnerable_duration", 10);

		config.addDefault("tool.role_weapon", "BOW");
		config.addDefault("tool.role_armor", "LEATHER_HELMET");

		this.first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		double first_skill_arrow_speed = config.getDouble("stats.first_skill_arrow_speed", 2.5);
		int first_skill_arrow_timeout = config.getInt("stats.first_skill_arrow_timeout", 1200);
		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 4);
		float second_skill_explosion_power = (float) config.getDouble("stats.second_skill_explosion_power", 2.0);
		double second_skill_backdash_multiplier = config.getDouble("stats.second_skill_backdash_multiplier", -2.0);
		double second_skill_backdash_y = config.getDouble("stats.second_skill_backdash_y", 0.3);
		long second_skill_invulnerable_duration = config.getLong("stats.second_skill_invulnerable_duration", 10L);

		Material weapon;
		try {
			weapon = Material.valueOf(config.getString("tool.role_weapon"));
		} catch (IllegalArgumentException e) {
			weapon = Material.BOW;
		}
		this.weapon_item = weapon;

		Material armor;
		try {
			armor = Material.valueOf(config.getString("tool.role_armor"));
		} catch (IllegalArgumentException e) {
			armor = Material.LEATHER_HELMET;
		}
		this.armor_item = armor;
		this.container = new Container(
			this.first_skill_hunger_cost,
				first_skill_arrow_speed,
				first_skill_arrow_timeout,
			this.second_skill_hunger_cost,
				second_skill_explosion_power,
				second_skill_backdash_multiplier,
				second_skill_backdash_y,
				second_skill_invulnerable_duration,
			this.getPlugin()
		);
	}

	@Override
	public void useFirstSkill(@NotNull Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.TARGET)) return;
		ItemStack emblem = p.getInventory().getItemInMainHand(); //TODO 특수탄 장전으로 엠뷸럼과 활의 기능 분리가 필요해보임
		if (p.getCooldown(emblem) > 0) return;
		if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;
		this.shockDart.execute(p, this.container);
		p.setCooldown(emblem, getCooldown1() * 20);
	}

	@Override
	public void useSecondSkill(@NotNull Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.RANGE)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;
		this.backDash.execute(p, this.container);
		p.setCooldown(tool, getCooldown2() * 20);
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&8&l궁수의 활"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setRoleDefault(this.getRole())
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&8&l엘프의 모자"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.setRoleDefault(this.getRole())
				.hideAllFlags()
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
           &0&l[ &6&l아처 가이드 &0&l ]&r
      
           &0아처는 원거리에서 치명적인
           &0화살을 날려 적의 접근을 허용치
           &0않는 &2&l백발백중&r&0의 사수입니다.
      
           &7&m-----------------
           &0&l[ &1&l전직 계보 &0&l ]&r
           &0- &82차 전직: &0저격수, 유격병, 사냥꾼
           &0- &83차 전직: &0윈드워커
           """;

		String page2 = String.format("""
           &0&l[ &2&l보유 스킬 &0&l ]&r
      
           &6&l▶ &0&l충격화살 &8[%d초]
           &0화살이 적중한 위치에 강력한
           &1&l번개&r&0를 소환하여 타격합니다.
      
           &6&l▶ &0&l백대시 &8[%d초]
           &0폭발의 반동을 이용해 신속하게
          &0&l후방&r&0으로 거리를 벌립니다.
          &7&m-----------------
          """, getCooldown1(), getCooldown2());

		return createGuideBook("아처", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}