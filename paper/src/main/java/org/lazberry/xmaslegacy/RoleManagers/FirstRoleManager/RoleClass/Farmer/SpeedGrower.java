package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Farmer;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Region.Region;
import org.lazberry.xmaslegacy.Region.RegionManager;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.List;

@Skill(type = PlayerSkills.SPEED_GROWER)
public class SpeedGrower implements Skills<Farmer.Container>, UsingEnergy {
	private final @NotNull RegionManager rm;

	@Inject
	public SpeedGrower(@NotNull RegionManager rm) {
		this.rm = rm;
	}

	@Override
    public boolean execute(@NotNull Player caster, @NotNull Farmer.@NotNull Container container) {
        List<Region> playerRegions = rm.getRegion(caster);

        if (playerRegions.isEmpty()) {
            InfoUtils.error(caster, "적절한 사용 조건이 아닙니다.");
            return false;
        }

        Location center = caster.getLocation();
        boolean success = false;

        for (int x = -container.second_skill_radius(); x <= container.second_skill_radius(); x++) {
            for (int y = -container.second_skill_y_range(); y <= container.second_skill_y_range(); y++) {
                for (int z = -container.second_skill_radius(); z <= container.second_skill_radius(); z++) {
                    Block block = center.clone().add(x, y, z).getBlock();

                    if (block.getBlockData() instanceof Ageable ageable) {
                        Region cropRegion = rm.getRegionAt(block.getLocation());

                        if (cropRegion != null && playerRegions.contains(cropRegion) && ageable.getAge() < ageable.getMaximumAge()) {

                            if (!success)
                                if (!consumeEnergy(caster, container.second_skill_hunger_cost())) return false;
                            ageable.setAge(ageable.getMaximumAge());
                            block.setBlockData(ageable);
                            block.getWorld().spawnParticle(
                                    Particle.HAPPY_VILLAGER,
                                    block.getLocation().add(0.5, 0.5, 0.5),
                                    container.second_skill_particle_count(),
                                    container.second_skill_particle_offset(),
                                    container.second_skill_particle_offset(),
                                    container.second_skill_particle_offset()
                            );
                            success = true;
                        }
                    }
                }
            }
        }

        if (success) {
            caster.getLocation().getWorld().playSound(caster.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
            return true;
        }

        // 💡 4. 범위 내에 성장시킬 수 있는 작물이 단 하나도 없었다면 실패 처리
        caster.sendMessage(ColorUtils.chat(Alert.RED + " 주변에 성장시킬 수 있는 작물이 없습니다."));
        caster.playSound(caster, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        return false;
    }

    @Override
    public @NotNull SkillSet type() {
        return BasicSkills.SPEED_GROWER;
    }

    @Override
    public @NotNull Role role() {
        return BasicRoles.FARMER;
    }
}