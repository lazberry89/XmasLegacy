package org.lazberry.xmaslegacy.mining.logics;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.mining.MineField;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.concurrent.ThreadLocalRandom;

@Data
@Registry.Include(type = ServerType.MAIN)
public class MineManager {
    public static final NamespacedKey key = KeyUtils.get("mine_tool");

    private final XmasLegacy plugin;
    private volatile World world;
    private volatile MineField externalMine;
    private volatile MineField internalMine;

    private volatile double chanceOfEmerald = 0.02;
    private volatile double chanceOfDiamond = 0.04;
    private volatile double chanceOfLazuli = 0.08;
    private volatile double chanceOfGold = 0.07;
    private volatile double chanceOfIron = 0.09;
    private volatile double chanceOfCoal = 0.1;
    private volatile double chanceOfStone = 0.6;

    @Inject
    public MineManager(XmasLegacy plugin) {
        this.plugin = plugin;
        this.world = Bukkit.getWorld("port");
    }

    public ItemStack tool() {
        return ItemBuilder.of(plugin, Material.IRON_PICKAXE)
                .setTag(key, true)
                .setUnbreakable()
                .build();
    }

    public boolean isInsideInternalField(Player player) {
        if (!player.getWorld().equals(world)) return true;
        return internalMine != null && internalMine.isInside(player.getLocation());
    }

    public boolean registerExternal(MineField field) {
        if (internalMine == null ||
                field.sizeHorizontal() > internalMine.sizeHorizontal()) {
            externalMine = field;
            return true;
        }
        return false;
    }

    public void forceExternal(MineField field) {
        externalMine = field;
    }

    public boolean registerInternal(MineField field) {
        if (externalMine == null ||
                field.sizeHorizontal() < externalMine.sizeHorizontal()) {
            internalMine = field;
            return true;
        }
        return false;
    }

    public void forceInternal(MineField field) {
        internalMine = field;
    }

    public Material randomOreByChance() {
        double totalWeight = chanceOfEmerald + chanceOfDiamond + chanceOfLazuli
                + chanceOfGold + chanceOfIron + chanceOfCoal + chanceOfStone;

        if (totalWeight <= 0) {
            return Material.STONE;
        }

        double randomValue = ThreadLocalRandom.current().nextDouble() * totalWeight;

        if ((randomValue -= chanceOfEmerald) < 0) return Material.EMERALD_ORE;
        if ((randomValue -= chanceOfDiamond) < 0) return Material.DIAMOND_ORE;
        if ((randomValue -= chanceOfLazuli) < 0) return Material.LAPIS_ORE;
        if ((randomValue -= chanceOfGold) < 0) return Material.GOLD_ORE;
        if ((randomValue -= chanceOfIron) < 0) return Material.IRON_ORE;
        if ((randomValue - chanceOfCoal) < 0) return Material.COAL_ORE;

        return Material.STONE;
    }

    public boolean isBreakable(Material material) {
        return material == Material.STONE || material.name().contains("_ORE");
    }

    public void resetExternal() {
        externalMine = null;
    }

    public void resetInternal() {
        internalMine = null;
    }
}
