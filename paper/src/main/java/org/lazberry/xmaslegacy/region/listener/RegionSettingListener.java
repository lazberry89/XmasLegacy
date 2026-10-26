package org.lazberry.xmaslegacy.region.listener;

import org.bukkit.Sound;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.region.RegionManager;
import org.lazberry.xmaslegacy.region.indicator.RegionDisplayManager;
import org.lazberry.xmaslegacy.region.screen.RegionDeleteInterface;
import org.lazberry.xmaslegacy.region.screen.RegionSettingInterface;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.Objects;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class RegionSettingListener implements Listener {
	private final RegionManager regionManager;

	@Inject
    public RegionSettingListener(RegionManager regionManager) {
	    this.regionManager = regionManager;
    }

	@EventHandler
	public void clickIndicatorDisplay(PlayerInteractEntityEvent e) {
		if (!(e.getRightClicked() instanceof Interaction interaction)) return;
		var player = e.getPlayer();

		Long value = KeyUtils.get(interaction, RegionDisplayManager.key, PersistentDataType.LONG);
		if (value == null) return;

		var region = regionManager.getRegion(value);
		if (region == null) {
			InfoUtils.error(player, "연결이 끊긴 인디케이터입니다.");
			interaction.remove();
			return;
		}
		var owner = region.getOwner();
		if (Objects.equals(owner, player.getUniqueId())) {
			player.openInventory(new RegionSettingInterface(region).getInventory());
			player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
		} else InfoUtils.error(player, "권한이 없습니다.");
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
