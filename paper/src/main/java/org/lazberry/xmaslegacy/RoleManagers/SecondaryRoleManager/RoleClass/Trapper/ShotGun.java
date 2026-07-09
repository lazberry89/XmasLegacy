package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Trapper;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.SkillSet;

import static org.lazberry.xmaslegacy.Roles.SecondaryRoles.TRAPPER;
import static org.lazberry.xmaslegacy.settings.SecondarySkillSet.SHOTGUN;

public class ShotGun implements Skills<Trapper.Container>, UsingEnergy {

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Trapper.@NotNull Container container) {
        Location eyeLoc = caster.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize();

        int rayCount = 10;
        double totalSpread = 45.0;
        double startAngle = -totalSpread / 2.0;
        double angleIncrement = totalSpread / (rayCount - 1);

        double maxDistance = 2.0;
        double stepSize = 0.2;
        int steps = (int) (maxDistance / stepSize);

        double damagePerPellet = 3.0;

        for (int i = 0; i < rayCount; i++) {
            double currentAngle = startAngle + (i * angleIncrement);

            Vector rayDir = direction.clone().rotateAroundY(Math.toRadians(currentAngle));
            Location rayLoc = eyeLoc.clone();

            for (int step = 0; step < steps; step++) {
                rayLoc.add(rayDir.clone().multiply(stepSize));
                rayLoc.getWorld().spawnParticle(Particle.SMALL_FLAME, rayLoc, 1, 0, 0, 0, 0.01);

                var entities = rayLoc.getWorld().getNearbyEntities(rayLoc, 0.3, 0.3, 0.3);
                for (var entity : entities) {
                    if (entity instanceof LivingEntity victim && entity != caster) {
                        victim.setNoDamageTicks(0);
                        victim.damage(damagePerPellet, caster);
                    }
                }
            }
        }
        eyeLoc.getWorld().playSound(eyeLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.6f);
        eyeLoc.getWorld().playSound(eyeLoc, Sound.ITEM_FLINTANDSTEEL_USE, 1.0f, 0.5f);

        return true;
    }

    @Override
    public @NotNull SkillSet type() {
        return SHOTGUN;
    }

    @Override
    public @NotNull Role role() {
        return TRAPPER;
    }
}
