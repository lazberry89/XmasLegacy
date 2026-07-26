package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Priest.Skill;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Priest.Priest;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.SkillEffectManager;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.STEROID)
@Registry.Exclude(type = ServerType.LOBBY)
public class Steroid implements Skills<Priest.Container>, UsingEnergy {
	private final @NotNull SkillEffectManager sem;
	private final @NotNull PartyManager pm;

	@Inject
	public Steroid(@NotNull SkillEffectManager sem, @NotNull PartyManager pm) {
		this.sem = sem;
		this.pm = pm;
	}

	@Override
	public boolean execute(@NotNull Player caster, Priest.@NotNull Container container) {
		if (!consumeEnergy(caster, container.second_skill_hunger_cost())) return false;
		int duration = container.second_skill_strength_duration();
		int amplifier = container.second_skill_strength_amplifier();

		Particle.DustOptions dust = new Particle.DustOptions(Color.YELLOW, 1.0f);
		sem.drawCircularLine(caster.getLocation().add(0, 0.2, 0),
				Particle.DUST, 7, false, 100, dust);
		double r = container.second_skill_radius();
		for (Entity ally : caster.getNearbyEntities(r, r, r)) {
			if (!(ally instanceof Player target)) continue;
			if (pm.isParty(caster.getUniqueId(), target.getUniqueId())) {
				target.removePotionEffect(PotionEffectType.STRENGTH);
				target.addPotionEffect(new PotionEffect(
						PotionEffectType.STRENGTH,
						duration,
						amplifier
				));
				GlowUtils.glow(target, NamedTextColor.YELLOW);
				Bukkit.getScheduler().runTaskLater(container.plugin(), () -> GlowUtils.clearGlow(target), 80L);
			}
		}
		return true;
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.STEROID;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.PRIEST;
	}
}
