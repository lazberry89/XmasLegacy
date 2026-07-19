package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Miner;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Skill(type = PlayerSkills.CHAIN_MINING)
public class ChainMining implements Skills<Miner.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Miner.@NotNull Container container) {
		Block targeted = caster.getTargetBlockExact(container.first_skill_target_range());
		if (targeted == null) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 해당 블록이 없습니다!"));
			return false;
		}
		if (!isOre(targeted)) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 광물이 아닙니다!"));
			return false;
		}
		Location targetLoc = targeted.getLocation();
		List<Block> ores = getNearbyBlock(targetLoc, container.first_skill_ore_chain_loop());
		if (ores.isEmpty()) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 주변에 광물이 없습니다!"));
			return false;
		}

		if (!consumeEnergy(caster, container.first_skill_hunger_cost())) return false;

		for (Block ore : ores) {
			ore.breakNaturally();
		}
		return true;
	}

	@SuppressWarnings("SameParameterValue")
	private @NotNull List<Block> getNearbyBlock(@NotNull Location loc, int loop) {
		List<Block> result = new ArrayList<>();
		Set<Block> visited = new HashSet<>();

		Block startBlock = loc.getBlock();
		visited.add(startBlock);
		result.add(startBlock);

		collectOres(loc, loop, result, visited);
		return result;
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean isOre(@NotNull Block block) {
		String typeName = block.getType().name();
		return typeName.contains("_ORE");
	}


	private void collectOres(@NotNull Location loc, int loop, @NotNull List<Block> result, @NotNull Set<Block> visited) {
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
	public @NotNull SkillSet type() {
		return BasicSkills.CHAIN_MINING;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.MINER;
	}
}
