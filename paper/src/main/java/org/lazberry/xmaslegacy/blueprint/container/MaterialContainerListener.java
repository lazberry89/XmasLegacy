package org.lazberry.xmaslegacy.blueprint.container;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.blueprint.BluePrint;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;

@Listeners
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class MaterialContainerListener implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p)) return;
		if (e.getClick() == ClickType.DOUBLE_CLICK) return;

		var inv = e.getClickedInventory();
		if (inv == null) return;
		if (!(inv.getHolder() instanceof MaterialContainer container)) return;

		int slot = e.getRawSlot();

		e.setCancelled(slot != 4);

		BluePrint bluePrint = container.getBluePrint();
		p.playSound(p, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
		switch (slot) {
			case 5 -> container.next();
			case 3 -> {
				var item = inv.getItem(4);

				if (item == null || item.getType().isAir()) {
					InfoUtils.error(p, "빈칸에 건축 재료를 올려주세요!");
					return;
				}
				if (bluePrint.isFullyCollected(item.getType())) {
					InfoUtils.warn(p, "해당 재료는 모두 수집되었습니다.");
					return;
				}
				boolean result = container.saveMaterial(item);
				if (result) InfoUtils.info(p, "아이템이 성공적으로 저장되었습니다.");
				else InfoUtils.error(p, "아이템 타입을 맞춰서 저장해주세요!");
			}
			case 2 -> container.giveBackSavedItem(p, container.getCurrentItem());
		}
	}

	@EventHandler
	public void giveBack(InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player p)) return;

		var inv = e.getInventory();
		if (!(inv.getHolder() instanceof MaterialContainer)) return;

		var item = inv.getItem(4);
		InventoryHelper.giveItemOrDrop(p, item);
	}
}
