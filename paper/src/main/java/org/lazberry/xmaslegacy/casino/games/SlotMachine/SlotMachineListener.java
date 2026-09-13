package org.lazberry.xmaslegacy.casino.games.SlotMachine;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class SlotMachineListener implements Listener {
    private final SlotMachineManager smm;

    @Inject
    public SlotMachineListener(SlotMachineManager smm) {
        this.smm = smm;
    }

    @EventHandler
    public void preventMachineTriggerBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block block = e.getBlock();
        if (p.isOp() || block.getType() != Material.LEVER) return;

        Location loc = block.getLocation();
        smm.getMachineByTrigger(loc).ifPresent(m -> {
            e.setCancelled(true);
            InfoUtils.error(p, "파괴할 수 없습니다. 우클릭으로 작동시켜주세요!");
        });
    }

    @EventHandler
    public void runSlotMachineByTrigger(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        Block block = e.getClickedBlock();
        if (block == null || block.getType().isAir()) return;
        if (block.getType() != Material.LEVER) return;

        Location loc = block.getLocation();
        smm.getMachineByTrigger(loc).ifPresent(m -> {
            e.setCancelled(true);
            smm.runSlotMachine(p, m.getName());
        });
    }
}
