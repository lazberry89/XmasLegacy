package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Miner;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.ArrayList;
import java.util.List;

@Skill(type = PlayerSkills.ORE_EYE)
public class OreEye implements Skills<Miner.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Miner.@NotNull Container container) {
		if (!consumeEnergy(caster, container.second_skill_hunger_cost())) return false;
		List<Block> result = new ArrayList<>();
		for (int i = -container.second_skill_scan_range(); i <= container.second_skill_scan_range(); i++) {
			for (int j = -container.second_skill_scan_range(); j <= container.second_skill_scan_range(); j++) {
				for (int k = -container.second_skill_scan_range(); k <= container.second_skill_scan_range(); k++) {
					Block block = caster.getLocation().clone().add(i, j, k).getBlock();
					if (isOre(block)) {
						result.add(block);
					}
				}
			}
		}
		result.forEach(b -> this.blockGlow(b, container));
		caster.playSound(caster.getLocation(), Sound.BLOCK_BELL_USE, 1.0f, 0.8f);
		return true;
	}

	private void blockGlow(@NotNull Block block, @NotNull Miner.@NotNull Container container) {
		Location loc = block.getLocation();
		loc.getWorld().spawn(loc, Shulker.class, s -> {
			s.setAI(false);
			s.setSilent(true);
			s.setInvulnerable(true);
			s.setInvisible(true);
			s.setCollidable(false);
			s.setPeek(0);
			GlowUtils.glow(s, NamedTextColor.RED);
			Bukkit.getScheduler().runTaskLater(container.plugin(), s::remove, container.second_skill_glow_duration());
		});
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private boolean isOre(@NotNull Block block) {
		String typeName = block.getType().name();
		return typeName.contains("_ORE");
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.ORE_EYE;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.MINER;
	}
}
