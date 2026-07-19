package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Farmer;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Skill;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Region.Region;
import org.lazberry.xmaslegacy.Region.RegionManager;
import org.lazberry.xmaslegacy.RoleManagers.Skills;
import org.lazberry.xmaslegacy.RoleManagers.UsingEnergy;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import org.lazberry.xmaslegacy.settings.PlayerSkills;
import org.lazberry.xmaslegacy.settings.SkillSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Skill(type = PlayerSkills.RADIUS_HARVEST)
public class RadiusHarvest implements Skills<Farmer.Container>, UsingEnergy {

    @Override
    public boolean execute(@NotNull Player caster, @NotNull Farmer.@NotNull Container container) {
        if (!consumeEnergy(caster, container.first_skill_hunger_cost())) return false;
        List<Region> playerRegions = RegionManager.INSTANCE.getRegion(caster);
        if (playerRegions.isEmpty()) return false;

        List<Block> crops = getFullyGrownCrops(caster, container.first_skill_radius());
        List<Item> droppedEntities = new ArrayList<>();
        for (Block block : crops) {
            Region cropRegion = RegionManager.INSTANCE.getRegionAt(block.getLocation());

            if (cropRegion != null && playerRegions.contains(cropRegion)) {
                for (ItemStack drop : block.getDrops()) {
                    Item itemEntity = block.getWorld().dropItemNaturally(block.getLocation(), drop);
                    droppedEntities.add(itemEntity);
                }
                block.setType(Material.AIR);
                block.getLocation().getWorld().playSound(block.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
            } else {
                caster.sendMessage(ColorUtils.chat(Alert.RED + " 적절한 사용 조건이 아닙니다."));
                caster.playSound(caster, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                return false;
            }
        }
        if (!droppedEntities.isEmpty())
            droppedEntities.stream().filter(Objects::nonNull)
                    .forEach(i -> GlowUtils.glow(i, NamedTextColor.RED));
        return true;
    }

    public @NotNull List<Block> getFullyGrownCrops(@NotNull Player player, int radius) {
        List<Block> grownCrops = new ArrayList<>();
        Location center = player.getLocation();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    Block block = center.clone().add(x, y, z).getBlock();

                    if (block.getBlockData() instanceof Ageable ageable) {
                        if (ageable.getAge() == ageable.getMaximumAge()) {
                            grownCrops.add(block);
                        }
                    }
                }
            }
        }
        return grownCrops;
    }

    @Override
    public @NotNull SkillSet type() {
        return BasicSkills.RADIUS_HARVEST;
    }

    @Override
    public @NotNull Role role() {
        return BasicRoles.FARMER;
    }
}
