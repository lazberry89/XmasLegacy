package xmasLegacy.FirstRoleManager.Merchant;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.Listeners;

@SuppressWarnings("FieldCanBeLocal")
@Listeners
public class StockListener implements Listener {
	private final MerchantStockInterface msi;

	public StockListener() {
		this.msi = MerchantStockInterface.getInstance();
	}

	@EventHandler
	public void onStockCheck(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p)) return;
		Component title = e.getView().title();
		if (!title.equals(msi.getTitle()) && !title.equals(msi.getTitleFarm()) && !title.equals(msi.getTitleMiner())) return;

		e.setCancelled(true);

		int slot = e.getRawSlot();
		if (title.equals(msi.getTitle())) {
			switch (slot) {
				case 2 -> msi.OpenFarmer(p);
				case 6 -> msi.OpenMiner(p);
			}
		} else if (title.equals(msi.getTitleFarm())) {
			switch (slot) {
				case 8 -> msi.OpenStock(p);
				case 0 -> {
					if (msi.getStock(Material.WHEAT) > 0) {
						msi.Submit(Material.WHEAT);
						p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
						p.updateInventory();
					} else {
						sendError(p);
					}
				}
				case 1 -> {
					if (msi.getStock(Material.TORCHFLOWER) > 0) {
						msi.Submit(Material.TORCHFLOWER);
						p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
						p.updateInventory();
					} else {
						sendError(p);
					}
				}
				default -> p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			}
		} else if (title.equals(msi.getTitleMiner())) {
			switch (slot) {
				case 8 -> msi.OpenStock(p);
				case 0 -> {
					if (msi.getStock(Material.COAL) > 0) {
						msi.Submit(Material.COAL);
						p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
						p.updateInventory();
					} else {
						sendError(p);
					}
				}
				case 1 -> {
					if (msi.getStock(Material.IRON_INGOT) > 0) {
						msi.Submit(Material.IRON_INGOT);
						p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
						p.updateInventory();
					} else {
						sendError(p);
					}
				}
				case 2 -> {
					if (msi.getStock(Material.GOLD_INGOT) > 0) {
						msi.Submit(Material.GOLD_INGOT);
						p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
						p.updateInventory();
					} else {
						sendError(p);
					}
				}
				case 3 -> {
					if (msi.getStock(Material.DIAMOND) > 0) {
						msi.Submit(Material.DIAMOND);
						p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
						p.updateInventory();
					} else {
						sendError(p);
					}
				}
				default -> p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			}
		}
	}
	/*
	@EventHandler
	public void MerchantPurchaseEvent(PlayerTradeEvent e) {
		Player p = e.getPlayer();
		ItemStack result = e.getTrade().getResult();
		List<ItemStack> ingredients = e.getTrade().getIngredients();
		boolean value = Boolean.TRUE.equals(result.getPersistentDataContainer().get(plugin.getNamespacedKey("merchant_money"), PersistentDataType.BOOLEAN));
		if (value) {

		}
	}
	*/
	private void sendError(Player p) {
		p.sendMessage(ColorUtils.chat(Alert.RED + " 제출할 재고가 없네요!"));
		p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
	}
}
