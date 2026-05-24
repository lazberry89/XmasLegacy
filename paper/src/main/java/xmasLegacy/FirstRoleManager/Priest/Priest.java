package xmasLegacy.FirstRoleManager.Priest;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration; // 💡 [추가] 설정 파일 연동을 위한 임포트
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.SkillEffectManager;
import xmasLegacy.Utils.GlowUtils;
import xmasLegacy.Utils.ItemBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Priest extends AbstractFirstRole {
	private final PartyManager PM;
	private final SkillEffectManager SEM;
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.COMPACT_HEAL);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	private Material weapon_item;
	private Material armor_item;
	private double weapon_attack_damage;
	private double armor_state_value;
	private double armor_toughness_value;
	private int first_skill_hunger_cost;
	private double first_skill_raytrace_range;
	private int first_skill_regen_duration;
	private int first_skill_regen_amplifier;
	private int second_skill_hunger_cost;
	private int second_skill_strength_duration;
	private int second_skill_strength_amplifier;
	private double second_skill_radius;
	private static Priest instance;

	public static Priest getInstance() {
		if (instance == null) instance = new Priest();
		return instance;
	}

	private Priest() {
		super(Roles.PRIEST);
		this.PM = PartyManager.getInstance();
		this.SEM = SkillEffectManager.getInstance();
		// 💡 [추가] 부모 데이터 바인딩 파이프라인 가동
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
		// 1. 성직자 전용 YAML 스탯 기본값 주입
		config.addDefault("stats.weapon_attack_damage", 5.0);
		config.addDefault("stats.armor_state_value", 5.0);
		config.addDefault("stats.armor_toughness_value", 5.0);

		config.addDefault("stats.first_skill_hunger_cost", 3);
		config.addDefault("stats.first_skill_raytrace_range", 15.0);
		config.addDefault("stats.first_skill_regen_duration", 100); // 5 * 20
		config.addDefault("stats.first_skill_regen_amplifier", 1);

		config.addDefault("stats.second_skill_hunger_cost", 3);
		config.addDefault("stats.second_skill_strength_duration", 100); // 5 * 20
		config.addDefault("stats.second_skill_strength_amplifier", 1);
		config.addDefault("stats.second_skill_radius", 5.0);

		config.addDefault("tool.role_weapon", "GOLDEN_SPEAR");
		config.addDefault("tool.role_armor", "GOLDEN_CHESTPLATE");

		// 2. 파일 변수 바인딩 수립
		this.weapon_attack_damage = config.getDouble("stats.weapon_attack_damage", 5.0);
		this.armor_state_value = config.getDouble("stats.armor_state_value", 5.0);
		this.armor_toughness_value = config.getDouble("stats.armor_toughness_value", 5.0);

		this.first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		this.first_skill_raytrace_range = config.getDouble("stats.first_skill_raytrace_range", 15.0);
		this.first_skill_regen_duration = config.getInt("stats.first_skill_regen_duration", 100);
		this.first_skill_regen_amplifier = config.getInt("stats.first_skill_regen_amplifier", 1);

		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 3);
		this.second_skill_strength_duration = config.getInt("stats.second_skill_strength_duration", 100);
		this.second_skill_strength_amplifier = config.getInt("stats.second_skill_strength_amplifier", 1);
		this.second_skill_radius = config.getDouble("stats.second_skill_radius", 5.0);

		// 3. 재질 에러 검증 및 캐싱
		Material weapon;
		try {
			weapon = Material.valueOf(config.getString("tool.role_weapon"));
		} catch (IllegalArgumentException e) {
			weapon = Material.GOLDEN_SPEAR;
		}
		this.weapon_item = weapon;

		Material armor;
		try {
			armor = Material.valueOf(config.getString("tool.role_armor"));
		} catch (IllegalArgumentException e) {
			armor = Material.GOLDEN_CHESTPLATE;
		}
		this.armor_item = armor;
	}

	@Override
	public void useFirstSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
			return;
		}

		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;
		int duration = this.first_skill_regen_duration;
		int amplifier = this.first_skill_regen_amplifier;
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		Entity entity = p.getTargetEntity((int) this.first_skill_raytrace_range, false);
		if (entity != null) {
			if (!(entity instanceof Player target) || !PM.isParty(p.getUniqueId(), target.getUniqueId())) {
				p.sendMessage(ColorUtils.chat(Alert.RED + " 유효한 타겟이 아닙니다!"));
				GlowUtils.setGlowColor(entity, NamedTextColor.RED);
				Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
					if (entity.isValid()) {
						GlowUtils.clearGlow(entity);
					}
				}, 10L);
				return;
			}
			target.removePotionEffect(PotionEffectType.REGENERATION);
			GlowUtils.setGlowColor(target, NamedTextColor.GREEN);
			target.addPotionEffect(new PotionEffect(
					org.bukkit.potion.PotionEffectType.REGENERATION,
					duration,
					amplifier
			));
			Bukkit.getScheduler().runTaskLater(getPlugin(), () -> GlowUtils.clearGlow(target), 80L);
			target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, p.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01);
			target.getWorld().playSound(p.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 1.0f, 1.0f);
			p.setCooldown(tool, this.getCooldown1() * 20);
		}
	}

	@Override
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;

		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
			return;
		}
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;
		int duration = this.second_skill_strength_duration;
		int amplifier = this.second_skill_strength_amplifier;

		Particle.DustOptions dust = new Particle.DustOptions(Color.YELLOW, 1.0f);
		SEM.drawCircularLine(p.getLocation().add(0, 0.2, 0),
				Particle.DUST, 7, false, 100, dust);
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		double r = this.second_skill_radius;
		for (Entity ally : p.getNearbyEntities(r, r, r)) {
			if (!(ally instanceof Player target)) continue;
			if (PM.isParty(p.getUniqueId(), target.getUniqueId())) {
				target.removePotionEffect(PotionEffectType.STRENGTH);
				target.addPotionEffect(new PotionEffect(
						PotionEffectType.STRENGTH,
						duration,
						amplifier
				));
				GlowUtils.setGlowColor(target, NamedTextColor.YELLOW);
				Bukkit.getScheduler().runTaskLater(getPlugin(), () -> GlowUtils.clearGlow(target), 80L);
			}
		}
		p.setCooldown(tool, this.getCooldown2() * 20);

	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.PRIEST;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&e&l힐링 스피어"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setTag("role_id", "priest")
				// 💡 하드코딩 제거 및 설정 파일 변수 적용
				.addAttribute(Attribute.ATTACK_DAMAGE, this.weapon_attack_damage, AttributeModifier.Operation.ADD_NUMBER)
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		// 💡 하드코딩 제거 및 설정 파일 변수 적용
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&e&l단단한 근육"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				// 💡 하드코딩 제거 및 설정 파일 변수 적용
				.setArmorState(this.armor_state_value, EquipmentSlotGroup.CHEST)
				.addAttribute(Attribute.ARMOR_TOUGHNESS, this.armor_toughness_value, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
				.setTag("role_id", "priest")
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
      
           &e&l▶ &0&l불꽃의 가호 &8[%d초]
           &0주변 동료들에게 &c&l힘의 근원&r&0을
           &0부여하여 전투력을 높여줍니다.
           &7&m-----------------
           """, getCooldown1(), getCooldown2());

		return createGuideBook("성직자", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}