package org.lazberry.xmaslegacy.blueprint;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Listeners
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class MaterialContainerListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p)) return;

		var inv = e.getClickedInventory();
		if (inv == null) return;
		if (!(inv.getHolder() instanceof MaterialContainer container)) return;

		int slot = e.getRawSlot();

		e.setCancelled(slot != 4);

		switch (slot) {
			case 5 -> {
				container.next();
				p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
			}
			case 3 -> {}
			case 2 -> {}
		}
	}
}
