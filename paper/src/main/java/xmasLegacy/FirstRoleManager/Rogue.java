package xmasLegacy.FirstRoleManager;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import xmasLegacy.Emblems.EmblemType;
import xmasLegacy.PlayerSkillUseEvent;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.Utils.ItemBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Rogue extends AbstractFirstRole {
	private final @NotNull SkillEffectManager SEM;
	private final @NotNull Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.DAGGER_RUSH);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}
	private Material weapon_item;
	private Material armor_item;
	private int first_skill_hunger_cost;
	private int first_skill_range;
	private double first_skill_speed;
	private double first_skill_y_velocity;
	private double first_skill_hit_range;
	private int first_skill_timeout_ticks;
	private int dagger_rush_hits;
	private double dagger_rush_damage;
	private int second_skill_hunger_cost;
	private long second_skill_duration;

	public Rogue() {
		super(Roles.ROGUE);
		this.SEM = SkillEffectManager.INSTANCE;
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
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

		this.first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		this.first_skill_range = config.getInt("stats.first_skill_range", 10);
		this.first_skill_speed = config.getDouble("stats.first_skill_speed", 2.5);
		this.first_skill_y_velocity = config.getDouble("stats.first_skill_y_velocity", 0.2);
		this.first_skill_hit_range = config.getDouble("stats.first_skill_hit_range", 2.0);
		this.first_skill_timeout_ticks = config.getInt("stats.first_skill_timeout_ticks", 20);
		this.dagger_rush_hits = config.getInt("stats.dagger_rush_hits", 5);
		this.dagger_rush_damage = config.getDouble("stats.dagger_rush_damage", 2.0);
		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 3);
		this.second_skill_duration = config.getLong("stats.second_skill_duration", 100L);

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
			armor = Material.IRON_BOOTS;
		}
		this.armor_item = armor;
	}

	@Override
	public void useFirstSkill(Player p) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.TARGET);
		Bukkit.getPluginManager().callEvent(skillUse);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getBoots();
		if (tool == null || tool.getType() == Material.AIR) return;
		Entity target = p.getTargetEntity(this.first_skill_range, false);

		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (target != null) {
			if (target instanceof LivingEntity le) {
				if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;

				Vector vector = p.getLocation().getDirection().normalize();
				p.setVelocity(vector.multiply(this.first_skill_speed).setY(this.first_skill_y_velocity));
				SEM.followParticle(p, Particle.DUST, 0.5, new Particle.DustOptions(Color.GRAY, 1.5f));

				p.setCooldown(tool, this.getCooldown1() * 20);

				new BukkitRunnable() {
					int timeout = 0;

					@Override
					public void run() {
						timeout++;

						if (p.getLocation().distance(le.getLocation()) <= first_skill_hit_range) {
							useDaggerRush(p, le);
							this.cancel();
							return;
						}

						if (timeout > first_skill_timeout_ticks || !p.isOnline() || le.isDead()) {
							this.cancel();
						}
					}
				}.runTaskTimer(getPlugin(), 0L, 1L);

				p.setCooldown(tool, this.getCooldown1() * 20);
			} else {
				p.sendMessage(ColorUtils.chat(Alert.RED + " 유효한 타겟이 아닙니다!"));
			}
		} else {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 타겟이 없습니다!"));
		}
	}

	private void useDaggerRush(Player player, LivingEntity target) {
		// 타겟 무적 틱 초기화 및 도트 데미지 틱 타이머 작동
		new BukkitRunnable() {
			int count = 0;

			@Override
			public void run() {
				// 💡 하드코딩 제거 및 설정 파일 변수 적용
				if (count >= dagger_rush_hits || !target.isValid() || target.isDead()) {
					this.cancel();
					return;
				}
				target.setNoDamageTicks(0);

				// 💡 하드코딩 제거 및 설정 파일 변수 적용
				target.damage(dagger_rush_damage, player);

				target.getWorld().spawnParticle(Particle.SWEEP_ATTACK, target.getLocation().add(0, 1, 0), 1);
				target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.5f);

				count++;
			}
		}.runTaskTimer(getPlugin(), 0L, 2L);
	}

	@Override
	public void useSecondSkill(Player p) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.RANGE);
		Bukkit.getPluginManager().callEvent(skillUse);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		ItemStack[] armorContents = p.getInventory().getArmorContents().clone();
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;
		Particle.DustOptions dust = new Particle.DustOptions(Color.GRAY, 5.0f);
		p.getWorld().spawnParticle(Particle.DUST, p.getLocation(), 160, 5, 3, 5, 0.01, dust);
		p.playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.8f);
		p.setInvisible(true);

		p.getInventory().setArmorContents(new ItemStack[4]);

		Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
			if (p.isValid()) {
				p.setInvisible(false);
				if (armorContents != null) {
					p.getInventory().setArmorContents(armorContents);
				}
			}
		}, this.second_skill_duration);
		p.setCooldown(tool, this.getCooldown2() * 20);
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.ROGUE;
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