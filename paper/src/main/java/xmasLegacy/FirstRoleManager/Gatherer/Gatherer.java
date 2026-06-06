package xmasLegacy.FirstRoleManager.Gatherer;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import xmasLegacy.Emblems.EmblemType;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.PlayerSkillUseEvent;
import xmasLegacy.Utils.GlowUtils;
import xmasLegacy.Utils.ItemBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode, unused")
public class Gatherer extends AbstractFirstRole {
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.ETERNAL_POSE);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	private Material weapon_item;
	private Material armor_item;
	private double weapon_movement_speed;
	private double armor_movement_speed;
	private int first_skill_hunger_cost;
	private int first_skill_target_range;
	private int first_skill_particle_count;
	private double first_skill_particle_offset;
	private double first_skill_particle_speed;
	private float first_skill_particle_size;
	private int second_skill_hunger_cost;
	private int second_skill_entity_range;
	private int second_skill_container_range;
	private long second_skill_glow_duration;
	private static Gatherer instance;

	public static Gatherer getInstance() {
		if (instance == null) instance = new Gatherer();
		return instance;
	}

	private Gatherer() {
		super(Roles.GATHERER);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
		config.addDefault("stats.weapon_movement_speed", 0.01);
		config.addDefault("stats.armor_movement_speed", 0.01);

		config.addDefault("stats.first_skill_hunger_cost", 3);
		config.addDefault("stats.first_skill_target_range", 7);
		config.addDefault("stats.first_skill_particle_count", 15);
		config.addDefault("stats.first_skill_particle_offset", 0.5);
		config.addDefault("stats.first_skill_particle_speed", 0.01);
		config.addDefault("stats.first_skill_particle_size", 1.5);

		config.addDefault("stats.second_skill_hunger_cost", 8);
		config.addDefault("stats.second_skill_entity_range", 12);
		config.addDefault("stats.second_skill_container_range", 8);
		config.addDefault("stats.second_skill_glow_duration", 40L);

		config.addDefault("tool.role_weapon", "COMPASS");
		config.addDefault("tool.role_armor", "GOLDEN_BOOTS");

		this.weapon_movement_speed = config.getDouble("stats.weapon_movement_speed", 0.01);
		this.armor_movement_speed = config.getDouble("stats.armor_movement_speed", 0.01);

		this.first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		this.first_skill_target_range = config.getInt("stats.first_skill_target_range", 7);
		this.first_skill_particle_count = config.getInt("stats.first_skill_particle_count", 15);
		this.first_skill_particle_offset = config.getDouble("stats.first_skill_particle_offset", 0.5);
		this.first_skill_particle_speed = config.getDouble("stats.first_skill_particle_speed", 0.01);
		this.first_skill_particle_size = (float) config.getDouble("stats.first_skill_particle_size", 1.5);

		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 8);
		this.second_skill_entity_range = config.getInt("stats.second_skill_entity_range", 12);
		this.second_skill_container_range = config.getInt("stats.second_skill_container_range", 8);
		this.second_skill_glow_duration = config.getLong("stats.second_skill_glow_duration", 40L);

		Material weapon;
		try {
			weapon = Material.valueOf(config.getString("tool.role_weapon"));
		} catch (IllegalArgumentException e) {
			weapon = Material.COMPASS;
		}
		this.weapon_item = weapon;

		Material armor;
		try {
			armor = Material.valueOf(config.getString("tool.role_armor"));
		} catch (IllegalArgumentException e) {
			armor = Material.GOLDEN_BOOTS;
		}
		this.armor_item = armor;
	}

	private ItemStack CompassBuilder(Block target, Player p) {
		ItemStack compass = ItemBuilder.of(getPlugin(), Material.COMPASS)
				.setName(ColorUtils.chat(String.format("&6&l%s의 이터널포스", p.getName())))
				.setLore(ColorUtils.chat("&7제멋대로인 포스의 위치를 알려줍니다."))
				.setTag("pose", p.getName())
				.build();
		CompassMeta meta = (CompassMeta) compass.getItemMeta();
		meta.setLodestone(target.getLocation());
		meta.setLodestoneTracked(false);
		compass.setItemMeta(meta);
		return compass;
	}

	@Override
	public void useFirstSkill(Player p) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.TARGET);
		Bukkit.getPluginManager().callEvent(skillUse);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getItemInMainHand();
		Block pose = p.getTargetBlockExact(this.first_skill_target_range);
		if (pose == null || pose.getType() != Material.SEA_LANTERN) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 해당 블록이 없습니다!"));
			return;
		}
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;
		p.getInventory().addItem(CompassBuilder(pose, p));

		Particle.DustTransition dust = new Particle.DustTransition(Color.AQUA, Color.WHITE, this.first_skill_particle_size);
		p.getWorld().spawnParticle(
				Particle.DUST_COLOR_TRANSITION,
				p.getLocation(),
				this.first_skill_particle_count,
				this.first_skill_particle_offset,
				this.first_skill_particle_offset,
				this.first_skill_particle_offset,
				this.first_skill_particle_speed,
				dust
		);
		p.getWorld().playSound(p.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
		p.setCooldown(tool, this.getCooldown1() * 20);
	}

	@Override
	public void useSecondSkill(Player p) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.RANGE);
		Bukkit.getPluginManager().callEvent(skillUse);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getBoots();
		Location loc = p.getLocation();
		if (tool == null || tool.getType() == Material.AIR) return;
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! &e" + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;

		Block block;
		p.getNearbyEntities(this.second_skill_entity_range, this.second_skill_entity_range, this.second_skill_entity_range).forEach(e -> {
			if (e instanceof LivingEntity le) {
				GlowUtils.setGlowColor(le, NamedTextColor.GOLD);
				Bukkit.getScheduler().runTaskLater(getPlugin(),t -> {
					if (le.isValid()) {
						GlowUtils.clearGlow(le);
					}
				}, this.second_skill_glow_duration);
			}
		});
		for (int i = -this.second_skill_container_range; i <= this.second_skill_container_range; i++) {
			for (int j = -this.second_skill_container_range; j <= this.second_skill_container_range; j++) {
				for (int k = -this.second_skill_container_range; k <= this.second_skill_container_range; k++) {
					block = loc.clone().add(i, j, k).getBlock();
					if (block.getType().isAir()) continue;
					if (block.getState() instanceof Container) {
						BlockGlow(block);
						block.getWorld().playSound(block.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
					}
				}
			}
		}
		p.setCooldown(tool, this.getCooldown2() * 20);
	}

	private boolean isContainer(@NotNull Block block) {
		Material container = block.getType();
		return container == Material.CHEST || container == Material.TRAPPED_CHEST ||
				container == Material.ENDER_CHEST || container == Material.DISPENSER ||
				container == Material.DROPPER || container == Material.HOPPER;
	}

	private void BlockGlow(Block block) {
		Location loc = block.getLocation();
		loc.getWorld().spawn(loc, Shulker.class, s -> {
			s.setAI(false);
			s.setSilent(true);
			s.setInvulnerable(true);
			s.setInvisible(true);
			s.setCollidable(false);
			s.setPeek(0);
			GlowUtils.setGlowColor(s, NamedTextColor.GOLD);
			Bukkit.getScheduler().runTaskLater(getPlugin(), s::remove, this.second_skill_glow_duration);
		});
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.GATHERER;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&6&l최후의 길잡이"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setRoleDefault(this.getRole())
				.hideAllFlags()
				.addAttribute(Attribute.MOVEMENT_SPEED, this.weapon_movement_speed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND)
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&6&l길잡이의 유물"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setRoleDefault(this.getRole())
				.hideAllFlags()
				.setUnbreakable()
				.addAttribute(Attribute.MOVEMENT_SPEED, this.armor_movement_speed, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.FEET)
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
      		&0&l[ &6&l수집가 가이드 &0&l ]&r
      
      		&0수집가는 금지된 유적을 탐사하며
      		&0세상에 알려지지 않은 &3&l비밀&r&0과
      		&0유물을 찾아내는 탐험가입니다.
      
      		&7&m-----------------
      		&0&l[ &1&l전직 계보 &0&l ]&r
      		&0- &82차 전직: &8&o준비 중
      		&0- &83차 전직: &8&o준비 중
      		""";

		String page2 = String.format("""
      		&0&l[ &2&l보유 스킬 &0&l ]&r
      
      		&6&l▶ &0&l회귀의 바늘 &8[%d초]
      		&0공간의 좌표를 &1&l이터널포스&r&0로
      		&0고정하여 회귀 지점을 설정합니다.
      
      		&6&l▶ &0&l에테르의 눈 &8[%d초]
      		&0에테르와 공명하여 주변의 적과
      		&d&l루트 상자&r&0를 꿰뚫어 봅니다.
      		&7&m-----------------
      		""", getCooldown1(), getCooldown2());

		return createGuideBook("수집가", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}