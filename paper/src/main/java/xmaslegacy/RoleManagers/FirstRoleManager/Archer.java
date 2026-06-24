package xmaslegacy.RoleManagers.FirstRoleManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration; // 💡 [추가] 설정 파일 로드를 위한 임포트
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import xmaslegacy.Annotation.Roles;
import xmaslegacy.Emblems.EmblemType;
import xmaslegacy.Utils.ItemBuilder;
import xmaslegacy.Utils.KeyUtils;

@Roles
public class Archer extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;
	private int first_skill_hunger_cost;
	private double first_skill_arrow_speed;
	private int first_skill_arrow_timeout;
	private int second_skill_hunger_cost;
	private float second_skill_explosion_power;
	private double second_skill_backdash_multiplier;
	private double second_skill_backdash_y;
	private long second_skill_invulnerable_duration;

	public Archer() {
		super(BasicRoles.ARCHER);
		this.loadRoleData(getRole().name().toLowerCase());
	}

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
		this.first_skill_arrow_speed = config.getDouble("stats.first_skill_arrow_speed", 2.5);
		this.first_skill_arrow_timeout = config.getInt("stats.first_skill_arrow_timeout", 1200);
		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 4);
		this.second_skill_explosion_power = (float) config.getDouble("stats.second_skill_explosion_power", 2.0);
		this.second_skill_backdash_multiplier = config.getDouble("stats.second_skill_backdash_multiplier", -2.0);
		this.second_skill_backdash_y = config.getDouble("stats.second_skill_backdash_y", 0.3);
		this.second_skill_invulnerable_duration = config.getLong("stats.second_skill_invulnerable_duration", 10L);

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
	}

	@Override
	public void useFirstSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.TARGET)) return;
		ItemStack bow = p.getInventory().getItemInMainHand(); //TODO 특수탄 장전으로 엠뷸럼과 활의 기능 분리가 필요해보임
		if (p.getCooldown(bow) > 0) return;
		if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;
		p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ARROW_SHOOT, 1.0f, 0.6f);

		Arrow arrow = p.launchProjectile(Arrow.class);
		arrow.setVelocity(p.getLocation().getDirection().multiply(this.first_skill_arrow_speed));
		arrow.setShooter(p);
		arrow.getPersistentDataContainer().set(KeyUtils.get("skill"), PersistentDataType.STRING, "archer_arrow");
		arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (!arrow.isValid() || arrow.isOnGround()) {
					this.cancel();
					return;
				}
				arrow.getWorld().spawnParticle(Particle.FLAME, arrow.getLocation(), 3, 0.05, 0.05, 0.05, 0.01);
			}
		}.runTaskTimer(getPlugin(), 0L, 1L);

		p.setCooldown(bow, getCooldown1() * 20);
		Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
			if (arrow.isValid()) {
				arrow.remove();
			}
		}, this.first_skill_arrow_timeout);
	}

	@Override
	public void useSecondSkill(Player p) {
		if (isSkillCancelled(p, this , emblem, EmblemType.RANGE)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;
		p.setInvulnerable(true);
		p.getWorld().createExplosion(p.getLocation(), this.second_skill_explosion_power, false, false);
		Vector vector = p.getLocation().getDirection();
		p.setVelocity(vector.multiply(this.second_skill_backdash_multiplier).setY(this.second_skill_backdash_y));
		Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
			if (p.isValid()) {
				p.setInvulnerable(false);
			}
		}, this.second_skill_invulnerable_duration);
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