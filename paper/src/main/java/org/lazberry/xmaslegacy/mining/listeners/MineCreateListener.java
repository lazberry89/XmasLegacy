package org.lazberry.xmaslegacy.mining.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.mining.logics.MineCreateManager;
import org.lazberry.xmaslegacy.mining.logics.MineManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.UUID;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class MineCreateListener implements Listener {
    private final MineCreateManager mcm;

    @Inject
    public MineCreateListener(MineCreateManager mcm) {
        this.mcm = mcm;
    }

    @EventHandler
    public void mineFieldSelection(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;

        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        var action = e.getAction();
        var tool = e.getItem();
        var block = e.getClickedBlock();
        if (tool == null ||
                !tool.getPersistentDataContainer().has(MineManager.key) || block == null) return;
        Location loc = block.getLocation();

        e.setCancelled(true);
        if (action == Action.LEFT_CLICK_BLOCK) {
            mcm.addFirstSelection(uuid, loc);
            InfoUtils.warn(p, "First location is selected");
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            mcm.addSecondSelection(uuid, loc);
            InfoUtils.warn(p, "Second location is selected");
        }
    }
}
