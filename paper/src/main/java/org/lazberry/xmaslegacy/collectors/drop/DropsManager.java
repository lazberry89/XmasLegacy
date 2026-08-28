package org.lazberry.xmaslegacy.collectors.drop;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.collectors.field.Field;
import org.lazberry.xmaslegacy.collectors.game.Session;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.CollectionHandler;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.Partition;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Registry.Include(type = ServerType.MAIN)
public class DropsManager {
    private final DropsRepository dr;

    public DropsRepository getRepository() { return dr; }

    @Inject
    public DropsManager(DropsRepository dr) {
        this.dr = dr;
    }

    public void hitProcess(Session session, Player player, Block block) {
        Field field = session.getField();
        Location loc = block.getLocation();
        loc.add(0.5, 0.5, 0.5);
        World world = field.getWorld();
        if (!field.isRunning()) return;

        if (field.isDropContainer(block)) {
            int current = field.reducePotHealth(loc);
            if (current == 0) {
                world.getBlockAt(loc).setType(Material.AIR);
                world.playSound(loc, Sound.BLOCK_DECORATED_POT_BREAK, 1.0f, 1.0f);
                world.spawnParticle(
                        Particle.BLOCK,
                        loc,
                        20,
                        0.3, 0.3, 0.3,
                        0.05,
                        Material.DECORATED_POT.createBlockData()
                );
                List<ItemStack> drops = dr.getRandomDrops(ThreadLocalRandom.current().nextInt(1, 4),
                        session.getDifficulty());
                drops.forEach(i ->
                    GlowUtils.glow(world.dropItemNaturally(loc, i), NamedTextColor.RED)
                );
                var breakEvent = new PlayerBreakContainerEvent(session, player, loc);
                Bukkit.getServer().getPluginManager().callEvent(breakEvent);
            }
            var event = new PlayerHitContainerEvent(session, player, loc, current);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }
    }

    public int calculatePriceOfInventory(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        return Arrays.stream(contents)
                .filter(dr::isLootItem)
                .mapToInt(dr::getPrice)
                .sum();
    }

    public int weightOfInventory(Player player) {
        var actualItems = Arrays.stream(player.getInventory().getContents())
                .filter(i -> i != null && !i.getType().isAir())
                .toList();
        Partition<ItemStack> lootItems = CollectionHandler.partition(actualItems, dr::isLootItem);

        int sumOfLoot = lootItems.matches().stream().mapToInt(dr::getWeight).sum();
        int sumOfRest = lootItems.unmatches().size();

        return sumOfLoot + sumOfRest;
    }
}
