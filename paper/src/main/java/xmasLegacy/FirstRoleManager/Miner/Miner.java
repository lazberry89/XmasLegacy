package xmasLegacy.FirstRoleManager.Miner;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import xmasLegacy.Emblems.EmblemType;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.PlayerSkillUseEvent;
import xmasLegacy.Utils.GlowUtils;
import xmasLegacy.Utils.ItemBuilder;

import java.util.*;

public class Miner extends AbstractFirstRole {
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.CHAIN_MINING);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	private Material weapon_item;
	private Material armor_item;
	private int first_skill_hunger_cost;
	private int first_skill_target_range;
	private int first_skill_ore_chain_loop;
	private int second_skill_hunger_cost;
	private int second_skill_scan_range;
	private long second_skill_glow_duration;
	private static volatile Miner instance;

	public static Miner getInstance() {
		if (instance == null) {
			synchronized (Miner.class) {
				if (instance == null) instance = new Miner();
			}
		}
		return instance;
	}

	private Miner() {
		super(Roles.MINER);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
		config.addDefault("stats.first_skill_hunger_cost", 3);
		config.addDefault("stats.first_skill_target_range", 7);
		config.addDefault("stats.first_skill_ore_chain_loop", 5);

		config.addDefault("stats.second_skill_hunger_cost", 3);
		config.addDefault("stats.second_skill_scan_range", 15);
		config.addDefault("stats.second_skill_glow_duration", 40L);

		config.addDefault("tool.role_weapon", "IRON_PICKAXE");
		config.addDefault("tool.role_armor", "IRON_CHESTPLATE");

		this.first_skill_hunger_cost = config.getInt("stats.first_skill_hunger_cost", 3);
		this.first_skill_target_range = config.getInt("stats.first_skill_target_range", 7);
		this.first_skill_ore_chain_loop = config.getInt("stats.first_skill_ore_chain_loop", 5);

		this.second_skill_hunger_cost = config.getInt("stats.second_skill_hunger_cost", 3);
		this.second_skill_scan_range = config.getInt("stats.second_skill_scan_range", 15);
		this.second_skill_glow_duration = config.getLong("stats.second_skill_glow_duration", 40L);

		Material weapon;
		try {
			weapon = Material.valueOf(config.getString("tool.role_weapon"));
		} catch (IllegalArgumentException e) {
			weapon = Material.IRON_PICKAXE;
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
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		Block targeted = p.getTargetBlockExact(this.first_skill_target_range);
		if (targeted != null) {
			Location targetLoc = targeted.getLocation();
			List<Block> ores = getNearbyBlock(targetLoc, this.first_skill_ore_chain_loop);
			if (ores.isEmpty()) {
				p.sendMessage(ColorUtils.chat(Alert.RED + " 주변에 광물이 없습니다!"));
				return;
			}
			if (!consumeEnergy(p, this.first_skill_hunger_cost)) return;
			for (Block ore : ores) {
				ore.breakNaturally();
			}
			p.setCooldown(tool, this.getCooldown1() * 20);
		} else {
			p.sendMessage(Alert.RED + " 해당 블록이 없습니다!");
		}
	}

	private boolean isOre(Block block) {
		String typeName = block.getType().name();
		return typeName.contains("_ORE");
	}

	@SuppressWarnings("SameParameterValue")
	private List<Block> getNearbyBlock(Location loc, int loop) {
		List<Block> result = new ArrayList<>();
		Set<Block> visited = new HashSet<>();
		collectOres(loc, loop, result, visited);

		return result;
	}

	private void collectOres(Location loc, int loop, List<Block> result, Set<Block> visited) {
		if (loop <= 0) return;

		for (int i = -1; i <= 1; i++) {
			for (int j = -1; j <= 1; j++) {
				for (int k = -1; k <= 1; k++) {
					if (i == 0 && j == 0 && k == 0) continue;

					Block block = loc.clone().add(i, j, k).getBlock();

					if (visited.contains(block) || !isOre(block)) continue;

					visited.add(block);
					result.add(block);

					collectOres(block.getLocation(), loop - 1, result, visited);
				}
			}
		}
	}

	@Override
	public void useSecondSkill(Player p) {
		PlayerSkillUseEvent skillUse = new PlayerSkillUseEvent(p, this, emblem, EmblemType.RANGE);
		Bukkit.getPluginManager().callEvent(skillUse);
		if (skillUse.isCancelled()) return;
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;

		if (p.getCooldown(tool) > 0) {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, this.second_skill_hunger_cost)) return;
		List<Block> result = new ArrayList<>();
		for (int i = -this.second_skill_scan_range; i <= this.second_skill_scan_range; i++) {
			for (int j = -this.second_skill_scan_range; j <= this.second_skill_scan_range; j++) {
				for (int k = -this.second_skill_scan_range; k <= this.second_skill_scan_range; k++) {
					Block block = p.getLocation().clone().add(i, j, k).getBlock();
					if (isOre(block)) {
						result.add(block);
					}
				}
			}
		}
		result.forEach(this::BlockGlow);
		p.playSound(p.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 0.8f);
		p.setCooldown(tool, this.getCooldown2() * 20);
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
			GlowUtils.setGlowColor(s, NamedTextColor.RED);
			Bukkit.getScheduler().runTaskLater(getPlugin(), s::remove, this.second_skill_glow_duration);
		});
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.MINER;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&l광부의 곡괭이"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.setRoleDefault(this.getRole())
				.hideAllFlags()
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&7&l철제 보호구"))
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
      		&0&l[ &8&l광부 가이드 &0&l ]&r
      	
      		&0광부는 대지의 깊은 곳에서
      		&0희귀한 자원을 찾아내 서버의
      		&0&l경제&r&0를 지탱하는 전문가입니다.
      
      		&7&m-----------------
      		&0&l[ &1&l전직 계보 &0&l ]&r
      		&0- &82차 전직: &8&o준비 중
      		&0- &83차 전직: &8&o준비 중
      		""";

		String page2 = String.format("""
      		&0&l[ &2&l보유 스킬 &0&l ]&r
      
      		&8&l▶ &0&l연쇄 광질 &8[%d초]
      		&0지맥의 흐름을 읽어 범위 내의
      		&0모든 광석을 &b&l한번에&r&0 채굴합니다.
      
      		&8&l▶ &0&l광부의 눈 &8[%d초]
      		&0숨겨진 광물을 감지하여 짧은
      		&0시간 동안 위치를 &e&l발광&r&0시킵니다.
      		&7&m-----------------
      		""", getCooldown1(), getCooldown2());

		// 부모 클래스의 메서드 활용 (2페이지 구성)
		return createGuideBook("광부", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}