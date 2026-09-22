package org.lazberry.xmaslegacy.blueprint;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

import java.util.*;
import java.util.function.Consumer;

@Getter
@ConsumableClass
public class BluePrint {
    private final String structureName;
    private final Map<Material, Integer> neededMaterial = new HashMap<>();
    private final Map<Material, Integer> remainingMaterial = new HashMap<>();
    private final Set<Material> materialTypes = new HashSet<>();
    private final List<RelativeBlock> blocks = new ArrayList<>();
    private int process = 0;
    private boolean lock = false;

    public BluePrint(String structureName) {
        this.structureName = structureName;
    }

    public int getRemainingAmount(Material material) {
        return remainingMaterial.getOrDefault(material, -1);
    }

    public int getNeededAmount(Material material) {
        return neededMaterial.getOrDefault(material, -1);
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
        materialTypes.add(material);
    }

    public int addBuildingMaterial(Material material, int amount) {
        if (!isBuildingMaterial(material) || amount <= 0) return amount;

        int max = getNeededAmount(material);
        int current = remainingMaterial.getOrDefault(material, 0);
        int space = max - current;

        if (space <= 0) return amount;

        int toAdd = Math.min(space, amount);
        remainingMaterial.put(material, current + toAdd);

        return amount - toAdd;
    }

    public boolean isBuildingMaterial(Material material) {
        return neededMaterial.containsKey(material);
    }

    public boolean isMaterialFullyCollected() {
        return neededMaterial.equals(remainingMaterial);
    }

    public int getMaxProcess() {
        return blocks.size();
    }

    public void consumeMaterial(Material material) {
        remainingMaterial.computeIfPresent(material, (mat, count) -> count > 1 ? count - 1 : null);
    }

    public void consumeMaterial(Material material, int amount) {
        remainingMaterial.computeIfPresent(material, (mat, count) -> count > amount ? count - amount : null);
    }

    public boolean isFullySupplied() {
        return remainingMaterial.isEmpty();
    }

    public ArrayList<Material> getOrderedMaterialList() {
        return new ArrayList<>(materialTypes);
    }

    public boolean processFrom(Location origin, int from, Consumer<Location> loc) {
        if (lock || from >= getMaxProcess()) return false;
        this.process = from;
        this.lock = true;
        return process(origin, loc);
    }

    public boolean process(Location origin, Consumer<Location> loc) {
        if (process >= getMaxProcess()) return false;
        placeBlockAt(origin, blocks.get(process++), loc);
        return true;
    }

    private void placeBlockAt(Location origin, RelativeBlock block, Consumer<Location> loc) {
        if (block == null || block.getMaterial().isAir()) return;

        Location buildLoc = origin.clone().add(block.getX(), block.getY(), block.getZ());
        var world = buildLoc.getWorld();

        world.setType(buildLoc, block.getMaterial());
        world.getBlockAt(buildLoc).setBlockData(block.getBlockData());
        loc.accept(buildLoc);
    }

    public int getProcess() {
        return Math.min(process, blocks.size());
    }
}
