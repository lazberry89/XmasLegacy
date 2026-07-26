package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Gatherer;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.settings.*;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;

@Skill(type = PlayerSkills.ETERNAL_POSE)
@Registry.Exclude(type = ServerType.LOBBY)
public class EternalPose implements Skills<Gatherer.Container>, UsingEnergy {

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Gatherer.@NotNull Container container) {
        Block pose = caster.getTargetBlockExact(container.first_skill_target_range());
        if (pose == null || pose.getType() != Material.SEA_LANTERN) {
            caster.sendMessage(ColorUtils.chat(Alert.RED + " 해당 블록이 없습니다!"));
            return false;
        }
        if (!consumeEnergy(caster, container.first_skill_hunger_cost())) return false;
        caster.getInventory().addItem(compassBuilder(pose, caster, container));

        Particle.DustTransition dust = new Particle.DustTransition(Color.AQUA, Color.WHITE, container.first_skill_particle_size());
        caster.getWorld().spawnParticle(
                Particle.DUST_COLOR_TRANSITION,
                caster.getLocation(),
                container.first_skill_particle_count(),
                container.first_skill_particle_offset(),
                container.first_skill_particle_offset(),
                container.first_skill_particle_offset(),
                container.first_skill_particle_speed(),
                dust
        );
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_CHIME, 1.0f, 1.0f);
        return true;
    }

    private @NotNull ItemStack compassBuilder(@NotNull Block target, @NotNull Player p, @NotNull Gatherer.@NotNull Container container) {
        ItemStack compass = ItemBuilder.of(container.plugin(), Material.COMPASS)
                .setName(ColorUtils.chat(String.format("&6&l%s의 이터널포스", p.getName())))
                .setLore(ColorUtils.chat("&7제멋대로인 포스의 위치를 알려줍니다."))
                .setTag("pose", p.getName())
                .build();
        CompassMeta meta = (CompassMeta) compass.getItemMeta();
        meta.setLodestone(target.getLocation());
        meta.setLodestoneTracked(false);
        compass.setItemMeta(meta);
        return compass;
    }

    @Override
    public @NotNull SkillSet type() {
        return BasicSkills.ETERNAL_POSE;
    }

    @Override
    public @NotNull Role role() {
        return BasicRoles.GATHERER;
    }
}
