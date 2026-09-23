package org.lazberry.xmaslegacy.blueprint;

import com.google.gson.Gson;
import io.th0rgal.oraxen.api.OraxenItems;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.*;
import java.util.function.Consumer;

@Getter
@ConsumableClass
public class BluePrint {
    private static final Gson GSON = new com.google.gson.GsonBuilder().create();
    private static final NamespacedKey key = KeyUtils.get("blueprint");

    public static boolean isBluePrint(ItemStack item) {
        return KeyUtils.hasKey(item, key);
    }

    public static String getNameByItem(ItemStack item) {
        if (!isBluePrint(item)) return "";
        return KeyUtils.get(item, key, PersistentDataType.STRING);
    }

    private final String structureName;
    private final Map<Material, Integer> neededMaterial = new HashMap<>();
    private final Map<Material, Integer> remainingMaterial = new HashMap<>();
    private final Set<Material> materialTypes = new HashSet<>();
    private final List<RelativeBlock> blocks = new ArrayList<>();
    private transient boolean lock = false;
    private int process = 0;

    public BluePrint(String structureName) {
        this.structureName = structureName;
    }

    public ItemStack getItem() {
        var builder = OraxenItems.getItemById("blueprint");
        var item = builder == null ? new ItemStack(Material.PAPER) : builder.build();

        return ItemBuilder.of(XmasLegacy.getInstance(), item)
                .setName(ColorUtils.chat("&9&l건축물 도면"))
                .setLore(
                        ColorUtils.chat("&7건축물: " + structureName),
                        ColorUtils.chat("&7우클릭 시 재료 보관함이 열리고, 수집 완료 후 우클릭 시"),
                        ColorUtils.chat("&7건축을 시작합니다."))
                .setTag(key, PersistentDataType.STRING, structureName)
                .setMaxStackSize(1)
                .build();
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

    public boolean isFullyCollected(Material material) {
        return remainingMaterial.containsKey(material) && neededMaterial.containsKey(material) &&
                Objects.equals(remainingMaterial.get(material), neededMaterial.get(material));
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

        var beforeBlock = buildLoc.getBlock();
        if (!beforeBlock.getType().isAir())
            beforeBlock.breakNaturally();

        world.setType(buildLoc, block.getMaterial());
        world.getBlockAt(buildLoc).setBlockData(block.getBlockData());
        loc.accept(buildLoc);
    }

    public void forEachVirtualLocation(Location origin, Consumer<Location> action) {
        if (origin == null || action == null || origin.getWorld() == null) return;

        var world = origin.getWorld();
        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();

        Location current = new Location(world, 0, 0, 0);

        for (RelativeBlock block : blocks) {
            if (block == null) continue;

            current.setX(originX + block.getX());
            current.setY(originY + block.getY());
            current.setZ(originZ + block.getZ());
            action.accept(current);
        }
    }

    public boolean matchAllVirtualLocations(Location origin, java.util.function.Predicate<Location> condition) {
        if (origin == null || condition == null || origin.getWorld() == null) return false;

        var world = origin.getWorld();
        int originX = origin.getBlockX();
        int originY = origin.getBlockY();
        int originZ = origin.getBlockZ();

        Location current = new Location(world, 0, 0, 0);

        for (RelativeBlock block : blocks) {
            if (block == null) continue;

            current.setX(originX + block.getX());
            current.setY(originY + block.getY());
            current.setZ(originZ + block.getZ());

            if (!condition.test(current)) {
                return false;
            }
        }
        return true;
    }

    public BluePrint copy() {
        BluePrint copy = new BluePrint(this.structureName);

        copy.blocks.addAll(this.blocks);

        copy.neededMaterial.putAll(this.neededMaterial);
        copy.remainingMaterial.putAll(this.remainingMaterial);
        copy.materialTypes.addAll(this.materialTypes);

        return copy;
    }

    public int getProcess() {
        return Math.min(process, blocks.size());
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static BluePrint parseInstanceFromJson(String json) {
        if (json == null || json.isEmpty()) return null;
        return GSON.fromJson(json, BluePrint.class);
    }
}
