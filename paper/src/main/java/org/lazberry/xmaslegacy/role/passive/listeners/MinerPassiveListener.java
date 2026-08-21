package org.lazberry.xmaslegacy.role.passive.listeners;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.Roles.ServerRoles;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.exp.ExpManager;
import org.lazberry.xmaslegacy.role.general.RoleManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.GlowUtils;

import java.util.concurrent.ThreadLocalRandom;

@Listeners
@Registry.Include(type = ServerType.WILD)
public class MinerPassiveListener extends PassiveListeners implements Listener {
    private final ItemStack cobbledDeepslate;
    private final ItemStack netherrack;
    private final ItemStack cobbleStone;
    private final ExpManager em;
    private final RoleManager rm;

    @Inject
    public MinerPassiveListener(UserManager um, ExpManager em, RoleManager rm) {
        super(ServerRoles.MINER, um);
        this.em = em;
        this.rm = rm;
        this.cobbledDeepslate = new ItemStack(Material.COBBLED_DEEPSLATE);
        this.netherrack = new ItemStack(Material.NETHERRACK);
        this.cobbleStone = new ItemStack(Material.COBBLESTONE);
    }

    private boolean isOre(Block block) {
        return block.getType().name().contains("ORE") ||
                block.getType().name().contains("ANCIENT_DEBRIS");
    }

    private int getExpByOreType(Material type) {
        String name = type.name();
        RoleManager.Miner m = rm.miner();

        if (name.contains("ANCIENT_DEBRIS")) {
            return ThreadLocalRandom.current().nextInt(m.getExpSpecialMin(), m.getExpSpecialMax() + 1);
        } else if (name.contains("DIAMOND") || name.contains("EMERALD")) {
            return ThreadLocalRandom.current().nextInt(m.getExpHighestMin(), m.getExpHighestMax() + 1);
        } else if (name.contains("GOLD") || name.contains("REDSTONE")) {
            return ThreadLocalRandom.current().nextInt(m.getExpHighMin(), m.getExpHighMax() + 1);
        } else if (name.contains("IRON") || name.contains("LAPIS") || name.contains("QUARTZ")) {
            return ThreadLocalRandom.current().nextInt(m.getExpMediumMin(), m.getExpMediumMax() + 1);
        } else if (name.contains("COAL") || name.contains("COPPER")) {
            return ThreadLocalRandom.current().nextInt(m.getExpLowMin(), m.getExpLowMax() + 1);
        }

        return ThreadLocalRandom.current().nextInt(m.getExpLowMin(), m.getExpLowMax() + 1);
    }

    @EventHandler
    public void onBreakOre(BlockBreakEvent e) {
        var player = e.getPlayer();
        Block block = e.getBlock();

        super.canUsePassive(player, u -> {
            var loc = block.getLocation();
            var name = block.getType().name();

            if (isOre(block)) {
                if (name.contains("DEEPSLATE")) {
                    loc.getWorld().dropItemNaturally(loc, cobbledDeepslate,
                            i -> GlowUtils.glow(i, NamedTextColor.GRAY));
                } else if (name.contains("NETHER")) {
                    loc.getWorld().dropItemNaturally(loc, netherrack,
                            i -> GlowUtils.glow(i, NamedTextColor.RED));
                } else {
                    if (name.contains("ANCIENT_DEBRIS")) return;
                    loc.getWorld().dropItemNaturally(loc, cobbleStone,
                            i -> GlowUtils.glow(i, NamedTextColor.GRAY));
                }
                int amount = getExpByOreType(block.getType());
                em.addRoleExp(player, amount);
                sendExpAlert(player, amount);
            }
            if (ThreadLocalRandom.current().nextDouble() < rm.miner().getSearchChance()) {
                findNearOre(block.getLocation());
                player.sendActionBar(ColorUtils.chat(Alert.YELLOW + " 광물 탐색기 발동됨"));
            }
        });

    }

    private void findNearOre(Location centerLoc) {
        World world = centerLoc.getWorld();
        if (world == null) return;

        RoleManager.Miner m = rm.miner();
        int radius = m.getSearchRadius();
        int glowDuration = m.getGlowDuration();

        int centerX = centerLoc.getBlockX();
        int centerY = centerLoc.getBlockY();
        int centerZ = centerLoc.getBlockZ();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    Block targetBlock = world.getBlockAt(centerX + x, centerY + y, centerZ + z);
                    if (isOre(targetBlock)) {
                        if (targetBlock.getType() == Material.ANCIENT_DEBRIS) continue;
                        Location targetLoc = targetBlock.getLocation();

                        GlowUtils.glowBlock(targetBlock, NamedTextColor.RED, glowDuration);
                        targetLoc.getWorld().playSound(targetLoc, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
                    }
                }
            }
        }
    }
}
