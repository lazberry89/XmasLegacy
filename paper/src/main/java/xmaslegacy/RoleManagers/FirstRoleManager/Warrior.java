package xmaslegacy.RoleManagers.FirstRoleManager;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Emblems.EmblemType;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.PlayerSkillUseEvent;
import xmaslegacy.Utils.GlowUtils;
import xmaslegacy.Utils.ItemBuilder;

@SuppressWarnings("DuplicatedCode")
@xmaslegacy.Annotation.Roles
public class Warrior extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;
	private double first_skill_usable_rate;
	private double first_skill_usable_higher_rate;
	private int first_skill_hunger_cost;
	private int first_skill_duration;
	private int first_skill_strength_amplifier;
	private int first_skill_strength_amplifier2;
	private int first_skill_speed_amplifier;
	private int second_skill_hunger_cost;
	private double second_skill_damage;

	public Warrior() {
		super(Roles.WARRIOR);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
		config.addDefault("stats.first_skill_usable_higher_rate", 0.25);
		config.addDefault("stats.first_skill_usable_rate", 0.5);
		config.addDefault("stats.first_skill_hunger_cost", 3);
		config.addDefault("stats.first_skill_duration", 60);
		config.addDefault("stats.first_skill_strength_amplifier2", 2);
		config.addDefault("stats.first_skill_strength_amplifier", 1);
		config.addDefault("stats.first_skill_speed_amplifier", 1);
		config.addDefault("stats.second_skill_hunger_cost", 3);
		config.addDefault("stats.second_skill_damage", 6.0);

		config.addDefault("tool.role_weapon", "IRON_AXE");
		config.addDefault("tool.role_armor", "IRON_CHESTPLATE");

		this.first_skill_usable_higher_rate = config.getDouble("first_skill_usable_higher_rate", 0.25);
		this.first_skill_usable_rate = config.getDouble("stats.first_skill_usable_rate", 0.5);
		this.first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		this.first_skill_duration = config.getInt("stats.first_skill_duration", 60);
		this.first_skill_strength_amplifier2 = config.getInt("stats.first_skill_strength_amplifier2", 2);
		this.first_skill_strength_amplifier = config.getInt("stats.first_skill_strength_amplifier", 1);
		this.first_skill_speed_amplifier = config.getInt("stats.first_skill_speed_amplifier", 1);
		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 3);
		this.second_skill_damage = config.getDouble("stats.second_skill_damage", 6.0);

		Material weapon;
		try {
			weapon = Material.valueOf(config.getString("tool.role_weapon"));
		} catch (IllegalArgumentException e) {
			weapon = Material.IRON_AXE;
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
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.TARGET);
		Bukkit.getPluginManager().callEvent(skillUse);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}

		AttributeInstance health = p.getAttribute(Attribute.MAX_HEALTH);
		if (health == null) return;
		double max = health.getBaseValue();

		if (p.getHealth() <= max * this.first_skill_usable_higher_rate) {
			if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;
			p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, this.first_skill_duration, this.first_skill_strength_amplifier2, true, true, false));
			p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, this.first_skill_duration, this.first_skill_speed_amplifier, true, false, false));
			warriorEffect(p);
			p.setCooldown(tool, getCooldown1() * 20);
		} else if (p.getHealth() <= max * this.first_skill_usable_rate) {
			if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;
			p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, this.first_skill_duration, this.first_skill_strength_amplifier, true, true, false));
			warriorEffect(p);
			p.setCooldown(tool, getCooldown1() * 20);
		} else {
			getPlugin().infoMsg(InfoLevel.ERROR, p, "체력수치가 조건 이상입니다.");
		}

	}

	private void warriorEffect(@NotNull Player p) {
		p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 15, 0.3, 0.5, 0.3, 0.01);
		GlowUtils.setGlowColor(p, NamedTextColor.DARK_RED);
		p.setSprinting(true);
		p.getWorld().playSound(p, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
		Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
			if (p.isValid()) {
				GlowUtils.clearGlow(p);
			}
		}, 60L);
	}

	@Override
	public void useSecondSkill(Player p) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.RANGE);
		Bukkit.getPluginManager().callEvent(skillUse);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat( Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}

		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;
		Location startLoc = p.getEyeLocation();
		final Vector direction = startLoc.getDirection().clone().normalize().multiply(1.0);
		final float playerYaw = p.getLocation().getYaw();

		ArmorStand axeStand = p.getWorld().spawn(startLoc, ArmorStand.class, stand -> {
			stand.setVisible(false);
			stand.setGravity(false);
			stand.setArms(true);
			stand.setBasePlate(false);
			stand.setMarker(true);
			GlowUtils.setGlowColor(stand, NamedTextColor.RED);

			Location loc = stand.getLocation();
			loc.setYaw(playerYaw);
			stand.teleport(loc);

			stand.getEquipment().setItemInMainHand(new ItemStack(weapon_item));
		});

		p.setCooldown(tool, this.getCooldown2() * 20);

		new BukkitRunnable() {
			int ticks = 0;
			final int maxTicks = 40;

			@Override
			public void run() {
				// 종료 조건: 시간 초과 또는 아머스탠드 소멸
				if (ticks >= maxTicks || !axeStand.isValid()) {
					if (axeStand.isValid()) axeStand.remove();
					this.cancel();
					return;
				}

				Location currentLoc = axeStand.getLocation().add(direction);
				axeStand.teleport(currentLoc);
				axeStand.getWorld().spawnParticle(Particle.SWEEP_ATTACK, currentLoc, 3, 0.05, 0.05, 0.05, 0.01);

				double rotation = ticks * 0.6;
				axeStand.setRightArmPose(new EulerAngle(rotation, 0, 0));

				for (Entity entity : axeStand.getNearbyEntities(1.2, 1.2, 1.2)) {
					if (entity instanceof LivingEntity target && !entity.equals(p)) {

						Location targetLoc = target.getLocation();
						Vector targetDir = targetLoc.getDirection().normalize();
						Location backLoc = targetLoc.clone().subtract(targetDir.multiply(1.5));

						if (backLoc.getBlock().getType().isSolid()) {
							backLoc.add(0, 1.0, 0);
						}

						p.teleport(backLoc);
						p.playSound(backLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.5f);
						target.damage(second_skill_damage, p);

						axeStand.remove();
						this.cancel();
						return;
					}
				}

				if (currentLoc.getBlock().getType().isSolid()) {
					axeStand.remove();
					this.cancel();
					return;
				}

				ticks++;
			}
		}.runTaskTimer(getPlugin(), 0L, 1L);
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.WARRIOR;
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
	public @NotNull ItemStack TargetEmblem() {
		return getEmblem().getTargetEmblem();
	}

	@Override
	public @NotNull ItemStack RangeEmblem() {
		return getEmblem().getRangeEmblem();
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
