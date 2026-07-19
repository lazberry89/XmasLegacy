package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Trapper;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.CHAIN_GRAB)
public class ChainGrab implements Skills<Trapper.Container>, UsingEnergy {
    private final @NotNull Particle.DustTransition trans;

    public ChainGrab() {
        this.trans = new Particle.DustTransition(Color.RED, Color.BLACK, 1.1f);
    }

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Trapper.@NotNull Container container) {
        if (!(consumeEnergy(caster, 3))) return false;
        caster.playSound(caster, Sound.BLOCK_CHAIN_PLACE, 1.0f, 1.0f);
        new BloodChainEffect(container.plugin()).playEffect(caster, target -> {
            Location casterLoc = caster.getLocation();
            Location targetLoc = target.getLocation();

            target.getWorld().spawnParticle(
                    Particle.DUST_COLOR_TRANSITION,
                    targetLoc.add(0, 1, 0),
                    20, 0.3, 0.5, 0.3, 0.05,
                    new Particle.DustTransition(Color.RED, Color.MAROON, 1.5f)
            );

            Vector pullVector = casterLoc.toVector().subtract(targetLoc.toVector());

            double horizontalPower = 2.5;
            pullVector.setY(0);
            pullVector.normalize().multiply(horizontalPower);

            double verticalPower = 0.45;
            pullVector.setY(verticalPower);

            target.setVelocity(pullVector);
            target.damage(5, caster);
            caster.getWorld().playSound(targetLoc, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 0.5f);
            target.getWorld().playSound(target, Sound.BLOCK_CHAIN_BREAK, 1.0f, 1.0f);
        }, () -> {
            if (caster.isValid() && caster.isOnline()) {
                caster.damage(3);
                caster.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, caster.getLocation(), 15, 0.5, 0.7, 0.5, 0.01, trans);
                caster.playSound(caster, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 0.6f);
            }
        });
        return true;
    }

    @Override
    public @NotNull SkillSet type() {
        return SecondarySkillSet.CHAIN_GRAB;
    }

    @Override
    public @NotNull Role role() {
        return SecondaryRoles.BERSERKER;
    }
}
