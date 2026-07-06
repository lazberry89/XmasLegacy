package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Priest.Skill;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Skill;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Priest.Priest;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.COMPACT_HEAL)
public class CompactHeal implements Skills<Priest.Container>, UsingEnergy {

	@Override
	public boolean execute(@NotNull Player caster, Priest.@NotNull Container container) {
		if (!consumeEnergy(caster, container.first_skill_hunger_cost())) return false;

		int duration = container.first_skill_regen_duration();
		int amplifier = container.first_skill_regen_amplifier();
		Entity entity = caster.getTargetEntity((int) container.first_skill_raytrace_range(), false);
		if (entity == null) return false;
		if (!(entity instanceof Player target) || !PartyManager.INSTANCE.isParty(caster.getUniqueId(), target.getUniqueId())) {
			caster.sendMessage(ColorUtils.chat(Alert.RED + " 유효한 타겟이 아닙니다!"));
			GlowUtils.glow(entity, NamedTextColor.RED);
			Bukkit.getScheduler().runTaskLater(container.plugin(), () -> {
				if (entity.isValid()) GlowUtils.clearGlow(entity);}, 10L);
			return false;
		}
		target.removePotionEffect(PotionEffectType.REGENERATION);
		GlowUtils.glow(target, NamedTextColor.GREEN);
		target.addPotionEffect(new PotionEffect(
				PotionEffectType.REGENERATION,
				duration,
				amplifier
		));
		Bukkit.getScheduler().runTaskLater(container.plugin(), () -> GlowUtils.clearGlow(target), 80L);
		target.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, target.getLocation().add(0, 1, 0), 10, 0.3, 0.3, 0.3, 0.01);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 1.0f, 1.0f);
		return true;
	}

	@Override
	public @NotNull SkillSet type() {
		return BasicSkills.COMPACT_HEAL;
	}

	@Override
	public @NotNull Role role() {
		return BasicRoles.PRIEST;
	}
}
