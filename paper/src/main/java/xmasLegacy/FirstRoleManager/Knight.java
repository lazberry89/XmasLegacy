package xmasLegacy.FirstRoleManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.entity.memory.MemoryKey;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.Emblems.EmblemType;
import xmasLegacy.PlayerSkillUseEvent;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.Utils.ItemBuilder;

import java.util.*;
@SuppressWarnings("DuplicatedCode")
public class Knight extends AbstractFirstRole {
	private float Damage = 5;
	private final SkillEffectManager SEM;
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.SHARP_SWEEPING);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	private Material weapon_item;
	private Material armor_item;
	private double armor_state_value;
	private int first_skill_hunger_cost;
	private double first_skill_speed;
	private double first_skill_y_velocity;
	private int first_skill_max_ticks;
	private double first_skill_range;
	private double first_skill_tick_y_add;
	private double first_skill_damage;
	private int first_skill_slow_duration;
	private int first_skill_slow_amplifier;
	private double first_skill_knockback_multiplier;
	private double first_skill_knockback_y;
	private int second_skill_hunger_cost;
	private double second_skill_range;
	private long second_skill_duration;
	private double second_skill_knockback;
	private double second_skill_knockback_y;
	private long second_skill_ai_restore_delay;
	private static Knight instance;

	public static Knight getInstance() {
		if (instance == null) instance = new Knight();
		return instance;
	}

	private Knight() {
		super(Roles.KNIGHT);
		this.SEM = SkillEffectManager.getInstance();
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
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

		this.Damage = (float) config.getDouble("stats.weapon_damage", 5.0);
		this.armor_state_value = config.getDouble("stats.armor_state_value", 7.0);

		this.first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		this.first_skill_speed = config.getDouble("stats.first_skill_speed", 1.5);
		this.first_skill_y_velocity = config.getDouble("stats.first_skill_y_velocity", 0.2);
		this.first_skill_max_ticks = config.getInt("stats.first_skill_max_ticks", 10);
		this.first_skill_range = config.getDouble("stats.first_skill_range", 1.5);
		this.first_skill_tick_y_add = config.getDouble("stats.first_skill_tick_y_add", 0.05);
		this.first_skill_damage = config.getDouble("stats.first_skill_damage", 5.0);
		this.first_skill_slow_duration = config.getInt("stats.first_skill_slow_duration", 20);
		this.first_skill_slow_amplifier = config.getInt("stats.first_skill_slow_amplifier", 2);
		this.first_skill_knockback_multiplier = config.getDouble("stats.first_skill_knockback_multiplier", 0.5);
		this.first_skill_knockback_y = config.getDouble("stats.first_skill_knockback_y", 0.2);

		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 3);
		this.second_skill_range = config.getDouble("stats.second_skill_range", 10.0);
		this.second_skill_duration = config.getLong("stats.second_skill_duration", 100L);
		this.second_skill_knockback = config.getDouble("stats.second_skill_knockback", -1.5);
		this.second_skill_knockback_y = config.getDouble("stats.second_skill_knockback_y", 0.15);
		this.second_skill_ai_restore_delay = config.getLong("stats.second_skill_ai_restore_delay", 3L);

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
	}

	@Override
	public void useFirstSkill(Player player) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(player, this, emblem, EmblemType.TARGET);
		if (skillUse.isCancelled()) return;
		ItemStack tool = player.getInventory().getItemInMainHand();
		if (player.getCooldown(tool) > 0) {
			player.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) player.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(player, this.first_skill_hunger_cost)) return;
		Vector direction = player.getLocation().getDirection().normalize();
		player.setVelocity(direction.multiply(this.first_skill_speed).setY(this.first_skill_y_velocity));

		player.setPose(Pose.SPIN_ATTACK);

		player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_THROW, 1, 1);
		new BukkitRunnable() {
			int ticks = 0;
			final int maxTicks = first_skill_max_ticks;
			final Set<UUID> hitEntities = new HashSet<>();

			@Override
			public void run() {
				if (ticks >= maxTicks || !player.isOnline()) {
					player.setPose(Pose.STANDING);
					this.cancel();
					return;
				}
				player.spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0), 1);
				player.setPose(Pose.SPIN_ATTACK);
				Vector currentV = player.getVelocity();
				player.setVelocity(currentV.add(new Vector(0, first_skill_tick_y_add, 0)));

				for (Entity entity : player.getNearbyEntities(first_skill_range, first_skill_range, first_skill_range)) {
					if (entity instanceof LivingEntity target && !entity.equals(player)) {

						if (hitEntities.contains(target.getUniqueId())) continue;

						target.damage(first_skill_damage, player);
						target.getWorld().playSound(target.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0F, 1.5F);
						target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, first_skill_slow_duration, first_skill_slow_amplifier, false, false, true));

						Vector push = direction.clone().multiply(first_skill_knockback_multiplier).setY(first_skill_knockback_y);
						target.setVelocity(push);

						hitEntities.add(target.getUniqueId());
					}
				}

				ticks++;
			}
		}.runTaskTimer(getPlugin(), 0L, 1L);
		player.setCooldown(tool, this.getCooldown1() * 20);
	}

	@Override
	public void useSecondSkill(Player p) { //Taunt
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.RANGE);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;

		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;
		p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 30, 10, 10, 10, 0.01);
		p.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_USE, 1.0f, 0.6f);

		for (Entity entity : p.getWorld().getNearbyEntities(p.getLocation(), this.second_skill_range, this.second_skill_range, this.second_skill_range)) {
			if (entity instanceof LivingEntity e && !p.equals(e)) {
				if (e instanceof Mob mob) {
					mob.setTarget(p);
					mob.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, mob.getLocation(), 10, 1.5, 1.5, 1.5, 0.1);
					Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
						if (mob.isValid()) {
							mob.setAI(false);
							mob.setMemory(MemoryKey.ANGRY_AT, null);
							mob.setMemory(MemoryKey.UNIVERSAL_ANGER, null);
							mob.setMemory(MemoryKey.GOLEM_DETECTED_RECENTLY, null);
							mob.setMemory(MemoryKey.HUNTED_RECENTLY, null);
							mob.setMemory(MemoryKey.DANGER_DETECTED_RECENTLY, null);
							mob.setTarget(null);
							Bukkit.getScheduler().runTaskLater(getPlugin(), () -> mob.setAI(true), second_skill_ai_restore_delay);
						}
					}, this.second_skill_duration);
				}
				if (e instanceof Player target) {
					SEM.knockbackEntity(p, target, this.second_skill_knockback, this.second_skill_knockback_y);
				}

			}
		}
		p.setCooldown(tool, this.getCooldown2() * 20);
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.KNIGHT;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&7&l녹슨 철검"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setItemModel("BasicSword")
				.setAttackDamage(this.Damage)
				.setTag("role_id", "knight")
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
				.setTag("role_id", "KnightArmor")
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