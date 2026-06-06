package xmasLegacy.FirstRoleManager;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import xmasLegacy.Emblems.EmblemType;
import xmasLegacy.PlayerSkillUseEvent;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.Utils.ItemBuilder;

import java.util.*;

@SuppressWarnings("DuplicatedCode")
public class Mage extends AbstractFirstRole {
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.COMPACT_INSANELY);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}
	private final SkillEffectManager SEM;

	private Material weapon_item;
	private Material armor_item;
	private int first_skill_hunger_cost;
	private double first_skill_speed;
	private int first_skill_max_ticks;
	private double first_skill_explosion_power;
	private double first_skill_slow_range_x;
	private double first_skill_slow_range_y;
	private double first_skill_slow_range_z;
	private int first_skill_slow_duration;
	private int first_skill_slow_amplifier;
	private int second_skill_hunger_cost;
	private double second_skill_distance;
	private Material second_skill_display_material;
	private int second_skill_max_ticks;
	private double second_skill_pull_strength;
	private double second_skill_pull_threshold;

	private static Mage instance;

	public static Mage getInstance() {
		if (instance == null) instance = new Mage();
		return instance;
	}

	private Mage() {
		super(Roles.MAGE);
		this.SEM = SkillEffectManager.getInstance();
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
		config.addDefault("stats.first_skill_hunger_cost", 6);
		config.addDefault("stats.first_skill_speed", 0.4);
		config.addDefault("stats.first_skill_max_ticks", 60);
		config.addDefault("stats.first_skill_explosion_power", 4.0);
		config.addDefault("stats.first_skill_slow_range_x", 1.3);
		config.addDefault("stats.first_skill_slow_range_y", 2.0);
		config.addDefault("stats.first_skill_slow_range_z", 1.3);
		config.addDefault("stats.first_skill_slow_duration", 20);
		config.addDefault("stats.first_skill_slow_amplifier", 1);

		config.addDefault("stats.second_skill_hunger_cost", 8);
		config.addDefault("stats.second_skill_distance", 8.0);
		config.addDefault("stats.second_skill_display_material", "PURPLE_STAINED_GLASS");
		config.addDefault("stats.second_skill_max_ticks", 50);
		config.addDefault("stats.second_skill_pull_strength", 0.35);
		config.addDefault("stats.second_skill_pull_threshold", 0.6);

		config.addDefault("tool.role_weapon", "BREEZE_ROD");
		config.addDefault("tool.role_armor", "DIAMOND_CHESTPLATE");

		this.first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 6);
		this.first_skill_speed = config.getDouble("stats.first_skill_speed", 0.4);
		this.first_skill_max_ticks = config.getInt("stats.first_skill_max_ticks", 60);
		this.first_skill_explosion_power = config.getDouble("stats.first_skill_explosion_power", 4.0);
		this.first_skill_slow_range_x = config.getDouble("stats.first_skill_slow_range_x", 1.3);
		this.first_skill_slow_range_y = config.getDouble("stats.first_skill_slow_range_y", 2.0);
		this.first_skill_slow_range_z = config.getDouble("stats.first_skill_slow_range_z", 1.3);
		this.first_skill_slow_duration = config.getInt("stats.first_skill_slow_duration", 20);
		this.first_skill_slow_amplifier = config.getInt("stats.first_skill_slow_amplifier", 1);

		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 8);
		this.second_skill_distance = config.getDouble("stats.second_skill_distance", 8.0);
		this.second_skill_max_ticks = config.getInt("stats.second_skill_max_ticks", 50);
		this.second_skill_pull_strength = config.getDouble("stats.second_skill_pull_strength", 0.35);
		this.second_skill_pull_threshold = config.getDouble("stats.second_skill_pull_threshold", 0.6);

		Material weapon;
		try {
			weapon = Material.valueOf(config.getString("tool.role_weapon"));
		} catch (IllegalArgumentException e) {
			weapon = Material.BREEZE_ROD;
		}
		this.weapon_item = weapon;

		Material armor;
		try {
			armor = Material.valueOf(config.getString("tool.role_armor"));
		} catch (IllegalArgumentException e) {
			armor = Material.DIAMOND_CHESTPLATE;
		}
		this.armor_item = armor;

		Material displayMat;
		try {
			displayMat = Material.valueOf(config.getString("stats.second_skill_display_material"));
		} catch (IllegalArgumentException e) {
			displayMat = Material.PURPLE_STAINED_GLASS;
		}
		this.second_skill_display_material = displayMat;
	}

	@Override
	public void useFirstSkill(Player p) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.RANGE);
		Bukkit.getPluginManager().callEvent(skillUse);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getItemInMainHand();

		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;
		Location startLoc = p.getEyeLocation();
		Vector dir = startLoc.getDirection().normalize().multiply(this.first_skill_speed);

		ArmorStand orb = p.getWorld().spawn(startLoc, ArmorStand.class, stand -> {
			stand.setVisible(false);
			stand.setMarker(true);
			stand.setGravity(false);
		});

		new BukkitRunnable() {
			int ticks = 0;
			@Override
			public void run() {
				if (ticks > first_skill_max_ticks || !orb.isValid() || orb.getLocation().getBlock().isSolid()) {
					explode(orb.getLocation(), p);
					orb.remove();
					this.cancel();
					return;
				}

				orb.teleport(orb.getLocation().add(dir));
				Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 1.0f);
				orb.getWorld().spawnParticle(Particle.DUST, orb.getLocation(), 15, 0.1, 0.1, 0.1, 0.02, dust);

				for (Entity e : orb.getNearbyEntities(1.0, 1.0, 1.0)) {
					if (e instanceof LivingEntity && !e.equals(p)) {
						explode(orb.getLocation(), p);
						orb.remove();
						this.cancel();
						return;
					}
				}
				ticks++;
			}
		}.runTaskTimer(getPlugin(), 0, 1);

		p.setCooldown(tool, getCooldown1() * 20);
	}

	private void explode(Location loc, Player source) {
		loc.getWorld().spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
		loc.getWorld().createExplosion(source, loc, (float) this.first_skill_explosion_power, false, false);
		for (Entity e : loc.getWorld().getNearbyEntities(loc, this.first_skill_slow_range_x, this.first_skill_slow_range_y, this.first_skill_slow_range_z)) {
			if (e instanceof LivingEntity le && !e.equals(source)) {
				le.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, this.first_skill_slow_duration, this.first_skill_slow_amplifier, true, false, false));
			}
		}
	}

	@SuppressWarnings("DuplicatedCode")
	@Override
	public void useSecondSkill(Player p) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.RANGE);
		Bukkit.getPluginManager().callEvent(skillUse);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;

		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}

		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;

		final Location center = p.getEyeLocation().add(p.getLocation().getDirection().multiply(this.second_skill_distance));

		if (center.getBlock().getType().isSolid()) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 해당 위치에 스킬을 사용할 수 없습니다!"));
			return;
		}
		List<BlockDisplay> cores = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			BlockDisplay bd = center.getWorld().spawn(center.clone().subtract(0.5, 0.5, 0.5), BlockDisplay.class, display -> {
				display.setBlock(this.second_skill_display_material.createBlockData());
				display.setBrightness(new Display.Brightness(15, 15));

				Transformation trans = display.getTransformation();
				trans.getScale().set(1.2f, 1.2f, 1.2f);
				display.setTransformation(trans);
				display.setInterpolationDuration(1);
				display.setInterpolationDelay(0);
			});
			cores.add(bd);
		}

		Particle.DustOptions dust = new Particle.DustOptions(Color.PURPLE, 1.0f);
		SEM.drawCircularLine(center, Particle.DUST, 3, true, 120, dust);
		SEM.drawCircularLine(center.clone().add(0, -0.5, 0), Particle.DUST, 2.5, true, 120, dust);
		SEM.drawCircularLine(center.clone().add(0, 0.5, 0), Particle.DUST, 2.5, true, 120, dust);

		p.getWorld().playSound(center, Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0f, 0.7f);

		p.setCooldown(tool, getCooldown2() * 20);

		new BukkitRunnable() {
			int ticks = 0;
			@Override
			public void run() {
				if (ticks > second_skill_max_ticks || !p.isOnline()) {
					cores.forEach(Entity::remove);
					this.cancel();
					return;
				}

				for (int i = 0; i < cores.size(); i++) {
					BlockDisplay bd = cores.get(i);
					Transformation trans = bd.getTransformation();

					float angle = (float) Math.toRadians(ticks * 15);
					if (i == 0) trans.getLeftRotation().set(new Quaternionf().rotationXYZ(angle, angle * 0.5f, 0));
					else if (i == 1) trans.getLeftRotation().set(new Quaternionf().rotationXYZ(0, angle, angle * 0.5f));
					else trans.getLeftRotation().set(new Quaternionf().rotationXYZ(angle * 0.5f, 0, angle));

					bd.setTransformation(trans);
					bd.setInterpolationDuration(1);
				}

				center.getWorld().spawnParticle(Particle.REVERSE_PORTAL, center, 10, 0.5, 0.5, 0.5, 0.1);
				for (Entity e : center.getWorld().getNearbyEntities(center, 6.0, 6.0, 6.0)) {

					if (e instanceof LivingEntity le && !e.equals(p)) {
						Vector direction = center.toVector().subtract(le.getLocation().toVector());

						double distance = direction.length();

						if (distance > second_skill_pull_threshold) {
							direction.normalize();
							le.setVelocity(direction.multiply(second_skill_pull_strength).setY(0.1));
						} else {
							le.setVelocity(new Vector(0, 0.02, 0));
						}
					}
				}

				ticks++;
			}
		}.runTaskTimer(getPlugin(), 0, 1);
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.MAGE;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&7&l일반 지팡이"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setAttackDamage(4)
				.setRoleDefault(this.getRole())
				.hideAllFlags()
				.addAttribute(Attribute.MOVEMENT_SPEED, -0.08, AttributeModifier.Operation.ADD_NUMBER)
				.setGlint(true)
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&7&l보호구"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.addAttribute(Attribute.JUMP_STRENGTH, 0.04, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
				.setArmorState(7, EquipmentSlotGroup.CHEST)
				.setRoleDefault(this.getRole())
				.build().clone();
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
           &0&l[ &5&l마법사 가이드 &0&l ]&r
      
           &0마법사는 원소의 힘을 빌려
           &0범위 내의 모든 적을 궤멸시키는
           &5&l강력한 마력&r&0의 소유자입니다.
      
           &7&m-----------------
           &0&l[ &1&l전직 계보 &0&l ]&r
           &0- &82차 전직: &0위자드, 엘리멘탈, 소환사
           &0- &83차 전직: &0아크메이지
           """;

		String page2 = String.format("""
           &0&l[ &2&l보유 스킬 &0&l ]&r
      
           &5&l▶ &0&l극점 &8[%d초]
           &0느리지만 파괴적인 &c&l중력구&r&0를
           &0사출하여 경로상의 적을 압살합니다.
      
           &5&l▶ &0&l중력장 &8[%d초]
           &0공간을 왜곡하여 주변의 적을
           &0&l중심점&r&0으로 강하게 끌어당깁니다.
           &7&m-----------------
           """, getCooldown1(), getCooldown2());

		return createGuideBook("마법사", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}