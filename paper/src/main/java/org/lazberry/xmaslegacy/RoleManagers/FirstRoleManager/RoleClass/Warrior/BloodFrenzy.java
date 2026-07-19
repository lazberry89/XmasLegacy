package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Warrior;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.BLOOD_FRENZY)
public class BloodFrenzy implements Skills<Warrior.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Warrior.@NotNull Container container) {
		AttributeInstance health = caster.getAttribute(Attribute.MAX_HEALTH);
		if (health == null) return false;
		double max = health.getBaseValue();

		if (caster.getHealth() <= max * container.first_skill_usable_higher_rate()) {
			if (!consumeEnergy(caster, container.first_skill_hunger_cost())) return false;
			caster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, container.first_skill_duration(), container.first_skill_strength_amplifier2(), true, true, false));
			caster.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, container.first_skill_duration(), container.first_skill_speed_amplifier(), true, false, false));
			warriorEffect(caster, container);
			return true;
		} else if (caster.getHealth() <= max * container.first_skill_usable_rate()) {
			if (!consumeEnergy(caster, container.first_skill_hunger_cost())) return false;
			caster.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, container.first_skill_duration(), container.first_skill_strength_amplifier(), true, true, false));
			warriorEffect(caster, container);
			return true;
		} else {
			InfoUtils.error(caster, "체력수치가 조건 이상입니다.");
			return false;
		}
	}

	private void warriorEffect(@NotNull Player p, @NotNull Warrior.@NotNull Container container) {
		p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 15, 0.3, 0.5, 0.3, 0.01);
		GlowUtils.glow(p, NamedTextColor.DARK_RED);
		p.setSprinting(true);
		p.getWorld().playSound(p, Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
		Bukkit.getScheduler().runTaskLater(container.plugin(), () -> {
			if (p.isValid()) {
				GlowUtils.clearGlow(p);
			}
		}, 60L);
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.BLOOD_FRENZY;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.WARRIOR;
	}
}
