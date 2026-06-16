package xmaslegacy.RoleManagers.FirstRoleManager.Priest;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Annotation.Roles;
import xmaslegacy.Emblems.EmblemType;
import xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import xmaslegacy.SkillEffectManager;
import xmaslegacy.Utils.GlowUtils;
import xmaslegacy.Utils.ItemBuilder;

@Roles
public class Priest extends AbstractFirstRole {
	private final @NotNull PartyManager pm;
	private final @NotNull SkillEffectManager sem;

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

	public Priest() {
		super(BasicRoles.PRIEST);
		this.pm = PartyManager.INSTANCE;
		this.sem = SkillEffectManager.INSTANCE;
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
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
		if (isSkillCancelled(p, this , emblem, EmblemType.TARGET)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;

		int duration = this.first_skill_regen_duration;
		int amplifier = this.first_skill_regen_amplifier;
		Entity entity = p.getTargetEntity((int) this.first_skill_raytrace_range, false);
		if (entity != null) {
			if (!(entity instanceof Player target) || !pm.isParty(p.getUniqueId(), target.getUniqueId())) {
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
		if (isSkillCancelled(p, this , emblem, EmblemType.RANGE)) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;
		int duration = this.second_skill_strength_duration;
		int amplifier = this.second_skill_strength_amplifier;

		Particle.DustOptions dust = new Particle.DustOptions(Color.YELLOW, 1.0f);
		sem.drawCircularLine(p.getLocation().add(0, 0.2, 0),
				Particle.DUST, 7, false, 100, dust);
		double r = this.second_skill_radius;
		for (Entity ally : p.getNearbyEntities(r, r, r)) {
			if (!(ally instanceof Player target)) continue;
			if (pm.isParty(p.getUniqueId(), target.getUniqueId())) {
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
	public @NotNull BasicRoles getRole() {
		return BasicRoles.PRIEST;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&e&l힐링 스피어"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setRoleDefault(this.getRole())
				.addAttribute(Attribute.ATTACK_DAMAGE, this.weapon_attack_damage, AttributeModifier.Operation.ADD_NUMBER)
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&e&l단단한 근육"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.hideAllFlags()
				.setArmorState(this.armor_state_value, EquipmentSlotGroup.CHEST)
				.addAttribute(Attribute.ARMOR_TOUGHNESS, this.armor_toughness_value, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST)
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