package xmasLegacy.FirstRoleManager.Miner;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.Utils.GlowUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.*;

public class Miner extends AbstractFirstRole {
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.CHAIN_MINING);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}

	public Miner(int c1, int c2, XmasLegacy plugin) {
		super(c1, c2, plugin);
	}

	@Override
	public void useFirstSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (p.getCooldown(tool.getType()) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
			return;
		}
		Block targeted = p.getTargetBlockExact(7);
		if (targeted != null) {
			Location targetLoc = targeted.getLocation();
			List<Block> ores = getNearbyBlock(targetLoc, 5);
			if (ores.isEmpty()) {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 주변에 광물이 없습니다!"));
				return;
			}
			if (!consumeEnergy(p, 3)) return;
			for (Block ore : ores) {
				ore.breakNaturally();
			}
			p.setCooldown(tool, this.getCooldown1() * 20);
		} else {
			p.sendMessage(Prefix.RED + " 해당 블록이 없습니다!");
			return;
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
		ItemStack tool = p.getInventory().getChestplate();
		if (tool == null || tool.getType() == Material.AIR) return;

		if (p.getCooldown(tool.getType()) > 0) {
			p.sendMessage(ColorUtils.chat(Prefix.RED + " 아직 스킬을 쓸 수 없습니다! " + (float) p.getCooldown(tool.getType()) / 20 + "&f초 기다리세요"));
			return;
		}
		if (!consumeEnergy(p, 3)) return;
		List<Block> result = new ArrayList<>();
		for (int i = -15; i <= 15; i++) {
			for (int j = -15; j <= 15; j++) {
				for (int k = -15; k <= 15; k++) {
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
			Bukkit.getScheduler().runTaskLater(getPlugin(), s::remove, 40L);
		});
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.MINER;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), Material.IRON_PICKAXE)
				.setName(ColorUtils.chat("&l광부의 곡괭이"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.setTag("role_id", "miner")
				.hideAllFlags()
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), Material.IRON_CHESTPLATE)
				.setName(ColorUtils.chat("&7&l철제 보호구"))
				.setLore(ColorUtils.chat("&e★☆☆☆☆☆☆&6☆☆&c☆"))
				.setUnbreakable()
				.setTag("role_id", "miner")
				.hideAllFlags()
				.build()
				.clone();
	}

	@Override
	public @NotNull ItemStack roleBook() {
		// [페이지 1] 광산의 깊이감이 느껴지는 직업 설명
		String page1 = """
          &8&l[ MINER ]
          
          &7광부는 거친 암석 속에서
          희귀 광물을 찾아내는 천부적인
          생산의 대가이자 자원의 주인입니다.
          
          &8&m-----------------
          &8&l[ ADVANCE ]
          &8- &72차: &8&o추후 공개 예정...
          &8- &73차: &8&o추후 공개 예정...
          """;

		// [페이지 2] 효율적인 채굴 스킬 설명
		String page2 = String.format("""
          &8&l[ SKILLS ]
          
          &8&l▶ 연쇄 광질 [%d초]
          &7지맥을 울려 범위 내의 모든
          &b&l광맥&r&7을 한 번에 채굴합니다.
          
          &8&l▶ 광부의 눈 [%d초]
          &7심연 속 숨겨진 광물들의
          &7위치를 꿰뚫어 발광시킵니다.
          
          &8&m-----------------
          """, getCooldown1(), getCooldown2());

		// 부모 클래스의 메서드 활용 (2페이지 구성)
		return createGuideBook("광부", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}
