package xmasLegacy.FirstRoleManager.Merchant;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.EconomyManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.Listeners;
import xmasLegacy.XmasLegacy;

import java.util.Map;

@Listeners
public class ShopListener implements Listener {
	private final PriceManager PIF;
	private final UserManager um;
	private final EconomyManager em;
	private boolean ignoreReset = false;

	public ShopListener() {
		this.PIF = PriceManager.getInstance();
		this.um = UserManager.getInstance();
		this.em = EconomyManager.getInstance();
	}

	@SuppressWarnings("DuplicatedCode")
	@EventHandler
	public void OwnerSetting(InventoryClickEvent e) {
		int slot = e.getRawSlot();
		if (!(e.getWhoClicked() instanceof Player p)) return;
		Inventory inv = e.getInventory();
		Component view = e.getView().title();
		if (view.equals(Constants.SHOP_TITLE)) {
			e.setCancelled(true);

			if (!PIF.getAvailableSlot().contains(slot)) return;
			Product prd = PIF.getProduct(slot);
			if (prd == null) {
				PIF.setSlot(slot);
				p.openInventory(PIF.PriceSet());
			} else {
				PIF.setPurchaseItem(prd);
				PIF.PurchaseInv(slot);
				PIF.setSlot(slot);
				p.openInventory(PIF.getPurchaseInv());
				p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
			}
		} else if (view.equals(Constants.PRICE_TITLE)) {
			switch (slot) {
				case 0 -> {
					e.setCancelled(true);
					ItemStack i = inv.getItem(4);
					if (i != null && i.getType() != Material.AIR) {
						Map<Integer, ItemStack> leftOver = p.getInventory().addItem(i);
						inv.setItem(4, null);
						PIF.removeProduct();
						if (!leftOver.isEmpty()) {
							for (ItemStack item : leftOver.values()) {
								p.getWorld().dropItemNaturally(p.getLocation(), item);
							}
						}
					}
					p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
					p.openInventory(PIF.MerchantShop());
				}
			case 3 -> {
				e.setCancelled(true);
				Product prd = PIF.getProduct(PIF.getSelectedSlot());
				if (prd == null) {
					p.sendMessage(ColorUtils.chat(Alert.RED + " 상품을 먼저 올려주세요!"));
					p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
				} else {
					prd.addPrice(500);
					PIF.reloadIcons();
					PIF.reloadShopIcons();
					p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
				}
			}

			case 4 ->
				Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(XmasLegacy.class), () -> {
					ItemStack itemInSlot = inv.getItem(4);
					if (itemInSlot == null || itemInSlot.getType() == Material.AIR) {
						PIF.removeProduct();
					} else {
						Product prd = new Product(itemInSlot, 500);
						PIF.setProduct(prd, prd.getPrice());
					}
					PIF.reloadIcons();
					PIF.reloadShopIcons();
				});
			case 5 -> {
				e.setCancelled(true);
				Product prd = PIF.getProduct(PIF.getSelectedSlot());
				if (prd == null) {
					p.sendMessage(ColorUtils.chat(Alert.RED + " 상품을 먼저 올려주세요!"));
					p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
				} else {
					if (prd.getPrice() < 500) {
						p.sendMessage(ColorUtils.chat(Alert.RED + " 가격이 너무 낮습니다!"));
						p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
						return;
					}
					prd.removePrice(500);
					PIF.reloadIcons();
					PIF.reloadShopIcons();
					p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
				}
			}
			case 8 -> {
				e.setCancelled(true);
				Product prd = PIF.getProduct(PIF.getSelectedSlot());
				if (prd == null) {
					p.sendMessage(ColorUtils.chat(Alert.RED + " 상품을 먼저 올려주세요!"));
					p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
				} else {
					e.getInventory().setItem(4, new ItemStack(Material.AIR));
					PIF.setProduct(prd, prd.getPrice());
					p.sendMessage(ColorUtils.chat(Alert.GREEN + " 상품이 등록되었습니다!"));
					ignoreReset = true;
					p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
					PIF.reloadShopIcons();
					Bukkit.getScheduler().runTaskLater(JavaPlugin.getPlugin(XmasLegacy.class),
							() -> p.openInventory(PIF.MerchantShop()), 1L);
				}
			}
				default -> {
					if (slot < 9) e.setCancelled(true);
				}
			}
		}
	}
	@EventHandler
	@SuppressWarnings("DuplicatedCode")
	public void onPurchase(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player p)) return;
		User user = um.getUser(p.getUniqueId());
		Inventory inv = e.getInventory();
		if (e.getView().title().equals(Constants.PURCHASE_TITLE)) {
			e.setCancelled(true);
			int slot = e.getRawSlot();
			Product prd = PIF.getPurchaseItem();
			if (prd == null) return;
			switch (slot) {
				case 12 -> {
					if (user.getDollars() < prd.getPrice()) {
						p.sendMessage(ColorUtils.chat(Alert.RED + " 돈이 부족합니다!"));
						p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
						return;
					}
					if (em.transferMoney(p.getUniqueId(), PIF.getOwner(), prd.getPrice())) {
						p.sendMessage(ColorUtils.chat(Alert.GREEN + " 상품을 구매하였습니다!"));
						p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
						Map<Integer, ItemStack> leftOver = p.getInventory().addItem(prd.getItem());
						if (!leftOver.isEmpty()) {
							for (ItemStack item : leftOver.values()) {
								p.getWorld().dropItemNaturally(p.getLocation(), item);
							}
						}
						PIF.removeProduct();
						PIF.removePurchaseItem();
						PIF.reloadShopIcons();
					} else {
						p.sendMessage(ColorUtils.chat(Alert.RED + " 구매에 실패하였습니다!"));
						p.sendMessage(ColorUtils.chat(Alert.RED + " 잔액부족 : &c" + user.getDollars()));
						p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
					}
					p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
					p.openInventory(PIF.MerchantShop());
				}
				case 14 -> {
					p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
					p.openInventory(PIF.MerchantShop());
				}
			}
		}
	}

	@SuppressWarnings("DuplicatedCode")
	@EventHandler
	public void onRunAwayWhilePrice(InventoryCloseEvent e) {
		if (!(e.getPlayer() instanceof Player p)) return;
		Inventory inv = e.getInventory();
		ItemStack item = inv.getItem(4);
		if (e.getView().title().equals(Constants.PRICE_TITLE)) {
			inv.setItem(4, new ItemStack(Material.AIR));
			if (!ignoreReset) {
				PIF.removeProduct();
			}
			ignoreReset = false;
			if (item != null && item.getType() != Material.AIR) {
				Map<Integer, ItemStack> leftOver = p.getInventory().addItem(item);
				if (!leftOver.isEmpty()) {
					for (ItemStack itemStack : leftOver.values()) {
						p.getWorld().dropItemNaturally(p.getLocation(), itemStack);
					}
				}
			}
		}
	}
}
