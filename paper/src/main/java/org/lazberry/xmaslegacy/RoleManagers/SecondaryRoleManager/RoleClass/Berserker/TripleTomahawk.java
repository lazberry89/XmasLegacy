package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.RoleClass.Berserker;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SecondarySkillSet;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Skill(type = PlayerSkills.TRIPLE_TOMAHAWK)
public class TripleTomahawk implements Skills<Berserker.Container>, UsingEnergy {

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Berserker.@NotNull Container container) {
        if (!consumeEnergy(caster, 3)) return false;
        Location loc = caster.getLocation().clone();
        new BukkitRunnable() {
            int shot = 0;

            @Override
            public void run() {
                if (shot >= 3) {
                    this.cancel();
                    return;
                }
                fireAxe(caster, container);
                loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);
                shot++;
            }
        }.runTaskTimer(container.plugin(), 0L, 6L);

        return true;
    }

    private @NotNull ArmorStand stand(Player p, Location startLoc, float playerYaw) {
        return p.getWorld().spawn(startLoc, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setGravity(false);
            stand.setArms(true);
            stand.setBasePlate(false);
            stand.setMarker(true);
            GlowUtils.glow(stand, NamedTextColor.DARK_RED);

            Location loc = stand.getLocation();
            loc.setYaw(playerYaw);
            stand.teleport(loc);
            stand.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_AXE));
        });
    }

    private void fireAxe(@NotNull Player p, @NotNull Berserker.@NotNull Container container) {
        Location startLoc = p.getEyeLocation();

        Vector direction = startLoc.getDirection().clone().normalize();
        direction.add(new Vector(
                (Math.random() - 0.5) * 0.15,
                (Math.random() - 0.5) * 0.15,
                (Math.random() - 0.5) * 0.15
        )).normalize();

        final float playerYaw = p.getLocation().getYaw();

        ArmorStand axeStand = stand(p, startLoc, playerYaw);

        new BukkitRunnable() {
            int ticks = 0;
            final int maxTicks = 40;
            final Set<UUID> hit = new HashSet<>();

            @Override
            public void run() {
                if (ticks >= maxTicks || !axeStand.isValid()) {
                    if (axeStand.isValid()) axeStand.remove();
                    this.cancel();
                    return;
                }

                Location currentLoc = axeStand.getLocation().add(direction);
                axeStand.teleport(currentLoc);
                axeStand.getWorld().spawnParticle(Particle.FLAME, currentLoc, 5, 0.01, 0.01, 0.01, 0);

                double rotation = ticks * 0.6;
                axeStand.setRightArmPose(new EulerAngle(rotation, 0, 0));

                for (Entity entity : axeStand.getNearbyEntities(1.1, 1.1, 1.1)) {
                    if (entity instanceof LivingEntity target
                            && !entity.equals(p)
                            && !hit.contains(entity.getUniqueId())) {

                        target.damage(5.0, p);
                        target.addPotionEffect(new PotionEffect(
                                PotionEffectType.SLOWNESS, 60, 1, true, true, false));
                        hit.add(entity.getUniqueId());

                        axeStand.remove();
                        this.cancel();
                        return;
                    }
                }

                if (currentLoc.getBlock().getType().isSolid()) {
                    axeStand.remove();
                    this.cancel();
                    return;
                }
                ticks++;
            }
        }.runTaskTimer(container.plugin(), 0L, 1L);
    }

    @Override
    public @NotNull SkillSet type() {
        return SecondarySkillSet.TRIPLE_TOMAHAWK;
    }

    @Override
    public @NotNull Role role() {
        return SecondaryRoles.BERSERKER;
    }
}
