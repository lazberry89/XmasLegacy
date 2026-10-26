package org.lazberry.xmaslegacy.region.indicator;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.region.Region;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.Objects;

@Registry.Exclude(type = ServerType.LOBBY)
public class RegionDisplayManager {
    private final BlockData beacon = Material.BEACON.createBlockData();
    private final NamespacedKey key;

    @Inject
    public RegionDisplayManager() {
        this.key = KeyUtils.get(Constants.regionKey);
    }

    public void spawnIndicatorDisplay(Region region) {
        var chunk = region.getChunk();
        if (chunk == null) {
            chunk = region.getWorld().getChunkAt(region.getChunkX(), region.getChunkZ());
        }
        if (chunk.isLoaded()) chunk.load(true);

        Location centerLoc = region.getTrueCenter(chunk).clone().add(0, 0.5, 0);
        World world = region.getWorld();

        world.spawn(centerLoc, BlockDisplay.class, b -> {
            b.setBlock(beacon);
            b.setGravity(false);
            b.setGlowColorOverride(Color.AQUA);
            b.customName(ColorUtils.chat("&b구역 : " + region.getId()));
            b.setCustomNameVisible(true);

            Transformation trans = b.getTransformation();
            float scale = 0.5f;
            trans.getScale().set(scale);
            trans.getTranslation().set(-scale / 2f, -scale / 2f, -scale / 2f);
            b.setTransformation(trans);

            KeyUtils.set(b, key, region.getKey());
            b.setInterpolationDuration(3);
            b.setInterpolationDelay(0);
            GlowUtils.glow(b, NamedTextColor.AQUA);

            region.setIndicator(b);
        });

        world.spawn(centerLoc, Interaction.class, i -> {
           i.setInteractionHeight(0.8f);
           i.setInteractionWidth(0.8f);
           i.setResponsive(true);
           KeyUtils.set(i, key, region.getKey());

           region.setInteraction(i);
        });
    }

    public boolean isIndicatorDisplay(Entity entity) {
        return KeyUtils.hasKey(entity, key);
    }

    public void removeDisplay(Region region) {
        var display = region.getIndicator();
        var interaction = region.getInteraction();

        removeProcess(region, display, BlockDisplay.class);
        removeProcess(region, interaction, Interaction.class);

        region.setIndicator(null);
        region.setInteraction(null);
    }

    private void removeProcess(Region region, Entity entity, Class<? extends Entity> clazz) {
        if (entity != null) {
            entity.remove();
        } else {
            var targets = region.getWorld().getEntitiesByClass(clazz)
                    .stream().filter(this::isIndicatorDisplay)
                    .filter(b -> Objects.equals(KeyUtils.get(b, key, PersistentDataType.LONG), region.getKey()))
                    .toList();
            targets.forEach(Entity::remove);
        }
    }

    /**
     * Not needed to find Region instance from Indicator and clear field about indicator
     * because manager clears region instances as well as indicator field cause it's transient.
     * Because of these, this method should be called only at server reload or plugin disable.
     */
    public void clearDisplays() {
        Bukkit.getWorlds().forEach(world -> {

            world.getEntitiesByClass(BlockDisplay.class).stream()
                    .filter(this::isIndicatorDisplay)
                    .forEach(Entity::remove);
            world.getEntitiesByClass(Interaction.class).stream()
                    .filter(this::isIndicatorDisplay)
                    .forEach(Entity::remove);
        });
    }
}
