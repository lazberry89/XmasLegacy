package org.lazberry.xmaslegacy.Region.Listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.Region.Gui.RegionDeleteInterface;
import org.lazberry.xmaslegacy.Region.Gui.RegionSettingInterface;
import org.lazberry.xmaslegacy.Region.RegionManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class RegionDeleteConfirmListener implements Listener {
    private final @NotNull RegionManager rm;

	@Inject
    public RegionDeleteConfirmListener(@NotNull RegionManager rm) {
		this.rm = rm;
    }

    @EventHandler
    public void deleteConfirming(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        Inventory inv = e.getClickedInventory();
        if (inv == null) return;

        if (!(inv.getHolder() instanceof RegionDeleteInterface rdi)) return;

        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (slot == 11) {
            var region = rdi.getRegion();
            rm.removeRegion(region);
            p.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            p.playSound(p, Sound.BLOCK_GLASS_BREAK, 1.0f, 1.4f);
        } else if (slot == 15) {
            p.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            p.openInventory(new RegionSettingInterface(rdi.getRegion()).getInventory());
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
        }
    }
}
