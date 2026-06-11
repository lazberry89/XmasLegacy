package xmasLegacy.Region.Listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import xmasLegacy.Listeners;
import xmasLegacy.Region.Gui.RegionDeleteInterface;
import xmasLegacy.Region.Gui.RegionSettingInterface;
import xmasLegacy.Region.RegionManager;

@Listeners
public class RegionDeleteConfirmListener implements Listener {
    private final @NotNull RegionManager rm;

    public RegionDeleteConfirmListener() {
        this.rm = RegionManager.getInstance();
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
