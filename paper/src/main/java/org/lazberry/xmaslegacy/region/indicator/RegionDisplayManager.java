package org.lazberry.xmaslegacy.region.indicator;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.region.Region;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

@Registry.Exclude(type = ServerType.LOBBY)
public class RegionDisplayManager {
    public static final NamespacedKey key = KeyUtils.get(Constants.regionKey);
    private final BlockData beacon = Material.BEACON.createBlockData();

    public RegionDisplayManager() {}

    public void spawnIndicatorDisplay(Region region) {
        if (region == null || region.getWorld() == null || !region.isValid()) return;

        World world = region.getWorld();
        var chunk = world.getChunkAt(region.getChunkX(), region.getChunkZ());
        // 청크가 언로드 상태이면 반드시 로드해야 엔티티가 정상 스폰되고 Y 좌표를 측정할 수 있습니다.
        if (!chunk.isLoaded()) {
            chunk.load(true);
        }

        // 기존에 혹시 남아있던 고아 엔티티가 있다면 먼저 깔끔하게 정리
        removeDisplay(region);

        Location centerLoc = region.getTrueCenter(chunk).clone().add(0, 0.5, 0);

        world.spawn(centerLoc, BlockDisplay.class, b -> {
            b.setBlock(beacon);
            b.setGravity(false);
            b.setGlowColorOverride(Color.AQUA);
            b.customName(ColorUtils.chat("&b구역 : " + region.getId()));
            b.setCustomNameVisible(true);

            float scale = 0.5f;
            Transformation trans = new Transformation(
                    new Vector3f(-scale / 2f, -scale / 2f, -scale / 2f),
                    b.getTransformation().getLeftRotation(),
                    new Vector3f(scale, scale, scale),
                    b.getTransformation().getRightRotation()
            );
            b.setTransformation(trans);

            KeyUtils.set(b, key, region.getKey());
            b.setInterpolationDuration(2);
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
        if (region == null) return;

        var display = region.getIndicator();
        var interaction = region.getInteraction();

        if (display != null && display.isValid()) {
            display.remove();
        }
        if (interaction != null && interaction.isValid()) {
            interaction.remove();
        }

        region.setIndicator(null);
        region.setInteraction(null);

        World world = region.getWorld();
        if (world != null) {
            var chunk = world.getChunkAt(region.getChunkX(), region.getChunkZ());
            if (!chunk.isLoaded()) {
                chunk.load(true);
            }

            for (Entity entity : chunk.getEntities()) {
                if (isIndicatorDisplay(entity)) {
                    Long storedKey = KeyUtils.get(entity, key, PersistentDataType.LONG);
                    if (storedKey != null && storedKey == region.getKey()) {
                        entity.remove();
                    }
                }
            }
        }
    }

    /**
     * 전체 월드에서 남은 인디케이터 디스플레이 및 인터랙션 엔티티를 청소합니다.
     * 플러그인 비활성화나 리로드 시 호출됩니다.
     */
    public void clearDisplays() {
        for (World world : Bukkit.getWorlds()) {
            world.getEntitiesByClass(BlockDisplay.class).stream()
                    .filter(this::isIndicatorDisplay)
                    .forEach(Entity::remove);
            world.getEntitiesByClass(Interaction.class).stream()
                    .filter(this::isIndicatorDisplay)
                    .forEach(Entity::remove);
        }
    }
}
