package org.lazberry.xmaslegacy.airdrop;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.Axiom;

@Listeners
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class AirdropListener implements Listener {
    private final AirdropManager airdropManager;

    @Inject
    public AirdropListener(AirdropManager airdropManager) {
        this.airdropManager = airdropManager;
    }

    @EventHandler
    public void removeChestWhenEmpty(InventoryCloseEvent e) {
        var inv = e.getInventory();
        var loc = inv.getLocation();

        if (loc == null) return;

        Block block = loc.getBlock();
        if (block.getType().isAir()) return;

        if (airdropManager.isSupplyChest(block) && inv.isEmpty()) {
            block.setType(Material.AIR);

            World world = loc.getWorld();
            var center = Axiom.centerBlock(loc);

            var blockData = airdropManager.getChestData();
            world.spawnParticle(Particle.BLOCK, center,
                    15, 0.3, 0.3, 0.3, 0.01, blockData);
            world.playSound(center, blockData.getSoundGroup().getBreakSound(), 1.0f, 1.0f);
        }
    }
}
