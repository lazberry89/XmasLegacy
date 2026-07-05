package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Rogue;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

public class Smoke implements Skills<Rogue.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Rogue.@NotNull Container container) {
		if (!consumeEnergy(caster, container.second_skill_hunger_cost())) return false;

		ItemStack[] armorContents = caster.getInventory().getArmorContents().clone();
		Particle.DustOptions dust = new Particle.DustOptions(Color.GRAY, 5.0f);
		caster.getWorld().spawnParticle(Particle.DUST, caster.getLocation(), 160, 5, 3, 5, 0.01, dust);
		caster.playSound(caster.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.8f);
		caster.setInvisible(true);

		caster.getInventory().setArmorContents(new ItemStack[4]);

		Bukkit.getScheduler().runTaskLater(container.plugin(), () -> {
			if (caster.isValid()) {
				caster.setInvisible(false);
				if (armorContents != null)
					caster.getInventory().setArmorContents(armorContents);
			}
		}, container.second_skill_duration());
		return true;
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.SMOKE;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.ROGUE;
	}
}
