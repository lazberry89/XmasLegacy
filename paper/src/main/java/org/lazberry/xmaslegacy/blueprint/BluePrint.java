package org.lazberry.xmaslegacy.blueprint;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
@ConsumableClass
public class BluePrint {
    private final String structureName;
    private final Map<Material, Integer> neededMaterial = new HashMap<>();
    private final List<RelativeBlock> blocks = new ArrayList<>();
    private int process = 0;

    public BluePrint(String structureName) {
        this.structureName = structureName;
    }

    public void addBlock(Location origin, Location blockLoc, @Nullable Block block) {
        if (block == null) return;

        var material = block.getType();
        if (material.isAir()) return;

        int relX = blockLoc.getBlockX() - origin.getBlockX();
        int relY = blockLoc.getBlockY() - origin.getBlockY();
        int relZ = blockLoc.getBlockZ() - origin.getBlockZ();

        blocks.add(new RelativeBlock(relX, relY, relZ, material, block.getBlockData()));
        neededMaterial.merge(material, 1, Integer::sum);
    }

    public int getMaxProcess() {
        return blocks.size();
    }

    public void process(Location origin, Consumer<Location> on) {
        if (process > getMaxProcess()) return;

        var block = blocks.get(Math.min(process++, blocks.size()));
        if (block == null || block.getMaterial().isAir()) return;

        Location buildLoc = origin.clone().add(block.getX(), block.getY(), block.getZ());
        var world = buildLoc.getWorld();

        world.setType(buildLoc, block.getMaterial());
        world.getBlockAt(buildLoc).setBlockData(block.getBlockData());
        on.accept(buildLoc);
    }

    public int getProcess() {
        return Math.min(process, blocks.size());
    }
}
