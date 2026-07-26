package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Gatherer;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.settings.SkillSet;

@Skill(type = PlayerSkills.TRUTH_EYE)
@Registry.Exclude(type = ServerType.LOBBY)
public class TruthEye implements Skills<Gatherer.Container>, UsingEnergy {

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Gatherer.@NotNull Container container) {
        if (!consumeEnergy(caster, container.second_skill_hunger_cost())) return false;

        Location loc = caster.getLocation();
        Block block;
        caster.getNearbyEntities(container.second_skill_entity_range(), container.second_skill_entity_range(), container.second_skill_entity_range()).forEach(e -> {
            if (e instanceof LivingEntity le) {
                GlowUtils.glow(le, NamedTextColor.GOLD);
                Bukkit.getScheduler().runTaskLater(container.plugin(), t -> {
                    if (le.isValid()) GlowUtils.clearGlow(le);
                }, container.second_skill_glow_duration());
            }
        });
        for (int i = -container.second_skill_container_range(); i <= container.second_skill_container_range(); i++) {
            for (int j = -container.second_skill_container_range(); j <= container.second_skill_container_range(); j++) {
                for (int k = -container.second_skill_container_range(); k <= container.second_skill_container_range(); k++) {
                    block = loc.clone().add(i, j, k).getBlock();
                    if (block.getType().isAir()) continue;
                    if (block.getState() instanceof Container) {
                        GlowUtils.glowBlock(block, NamedTextColor.GOLD, 10);
                        block.getWorld().playSound(block.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public @NotNull SkillSet type() {
        return BasicSkills.TRUTH_EYE;
    }

    @Override
    public @NotNull Role role() {
        return BasicRoles.GATHERER;
    }
}
