package xmasLegacy.Region;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class RegionSettingListener implements Listener {
    private final @NotNull RegionManager rm;

    public RegionSettingListener() {
        this.rm = RegionManager.getInstance();
    }

    @EventHandler
    public void SettingClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        Inventory inv = e.getClickedInventory();
        if (inv == null) return;

        if (!(inv.getHolder() instanceof RegionSettingInterface rsi)) return;

        e.setCancelled(true);
        int slot = e.getRawSlot();
        var region = rsi.getRegion();

        switch (slot) {
            case 0 -> {}
            case 2 -> {
                region.toggleEntry();
                p.playSound(p, Sound.BLOCK_TRIPWIRE_CLICK_ON, 1.0f, 1.0f);
                p.updateInventory();
            }
            case 3 -> {
                region.toggleInteraction();
                p.playSound(p, Sound.BLOCK_TRIPWIRE_CLICK_ON, 1.0f, 1.0f);
                p.updateInventory();
            }
            case 8 -> p.openInventory(new RegionDeleteInterface(region).getInventory());
        }
    }
}
