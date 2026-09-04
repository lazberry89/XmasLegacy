package org.lazberry.xmaslegacy.teleporter.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.teleporter.logic.TeleporterManager;
import org.lazberry.xmaslegacy.teleporter.logic.TeleporterCreateManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;

@Listeners
@Registry.Include(type = ServerType.GLOBAL)
public class TeleporterCreateListener implements Listener {
    private final TeleporterCreateManager tcm;
    private final TeleporterManager tm;

    @Inject
    public TeleporterCreateListener(TeleporterCreateManager tcm, TeleporterManager tm) {
        this.tcm = tcm;
        this.tm = tm;
    }

    @EventHandler
    public void portalFieldSelection(PlayerInteractEvent e) {
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        Player p = e.getPlayer();
        var uuid = p.getUniqueId();
        var action = e.getAction();

        if (tm.isTool(e.getItem())) {
            e.setCancelled(true);
            var block = e.getClickedBlock();
            if (block == null) return;
            var loc = block.getLocation();

            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();

            if (action == Action.LEFT_CLICK_BLOCK) {
                tcm.setFirstLoc(uuid, loc);
                InfoUtils.info(p, "First location selected. ({}, {}, {})", x, y ,z);
            } else if (action == Action.RIGHT_CLICK_BLOCK) {
                tcm.setSecondLoc(uuid, loc);
                InfoUtils.info(p, "Second location selected ({}, {}, {})", x, y, z);
            }
        }
    }

    @EventHandler
    public void portalDestinationSelection(PlayerToggleSneakEvent e) {
        var p = e.getPlayer();
        if (!e.isSneaking()) return;
        var item = p.getInventory().getItemInMainHand();
        if (item.getType().isAir()) return;

        if (tm.isTool(item)) {
            var loc = p.getLocation();
            tcm.setDestination(p.getUniqueId(), loc);
            InfoUtils.info(p, "Destination Selected. ({}, {}, {})", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
        }
    }
}
