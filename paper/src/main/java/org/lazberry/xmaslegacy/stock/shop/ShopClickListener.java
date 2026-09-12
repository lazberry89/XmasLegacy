package org.lazberry.xmaslegacy.stock.shop;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.stock.Stock;
import org.lazberry.xmaslegacy.stock.StockManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.Optional;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class ShopClickListener implements Listener {
	private final StockShopManager ssm;
	private final StockManager sm;

	@Inject
	public ShopClickListener(StockShopManager ssm, StockManager sm) {
		this.ssm = ssm;
		this.sm = sm;
	}

	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player player)) return;

		Inventory inv = e.getClickedInventory();
		if (inv == null) return;

		int slot = e.getRawSlot();
		if (inv.getHolder() instanceof StockShop shop) {
			e.setCancelled(true);

			Optional<ItemStack> optional = shop.getItemBySlot(slot);
			optional.ifPresent(cert -> ssm.select(player, cert));
		}

		if (inv.getHolder() instanceof StockSelectedShop select) {
			e.setCancelled(true);

			Stock stock = select.getStock();

			switch (slot) {
				case 0 -> {
					ssm.openShop(player);
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
				}
				case 3 -> {
					select.reduceAmount();
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
				}
				case 5 -> {
					select.addAmount();
					player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
				}
				case 8 -> {
					int amount = select.getAmount();
					switch (sm.buyStock(player, stock, amount)) {
						case SUCCESS -> {
							InfoUtils.info(player, "주식 {} {}주 구매에 성공하였습니다.", stock.getName(), amount);
							player.closeInventory();
						}
						case NOT_APPROPRIATE -> InfoUtils.error(player, "잔액이 부족합니다!");
						case TIMEOUT -> InfoUtils.error(player, "주식시장이 닫혔습니다. 기다려주세요!");
						default -> InfoUtils.error(player, "구매에 실패했습니다.");
					}
				}
			}
		}
	}
}
