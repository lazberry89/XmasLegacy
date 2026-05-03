package xmasLegacy.FirstRoleManager.Merchant;

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
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Prefix;
import xmasLegacy.XmasLegacy;

import java.util.Map;

@SuppressWarnings("ClassCanBeRecord")
public class ShopListener implements Listener {
	private final PriceInterface PIF;
	private final UserManager um;

	public ShopListener(PriceInterface PIF, UserManager um) {
		this.PIF = PIF;
		this.um = um;
	}

	@EventHandler
	public void OwnerSetting(InventoryClickEvent e) {
		int slot = e.getRawSlot();
		if (!(e.getWhoClicked() instanceof Player p)) return;
		Inventory inv = e.getInventory();
		if (e.getInventory().equals(PIF.MerchantShop())) {
			e.setCancelled(true);

			if (!PIF.getAvailableSlot().contains(slot)) return;
			Product prd = PIF.getProduct(slot);
			if (prd == null) {
				PIF.setSlot(slot);
				p.openInventory(PIF.PriceSet());
			}
		} else if (e.getInventory().equals(PIF.PriceSet())) {
			switch (slot) {
				case 0 -> {
					e.setCancelled(true);
					p.openInventory(PIF.MerchantShop());
					ItemStack i = inv.getItem(4);
					if (i != null && i.getType() != Material.AIR) {
						Map<Integer, ItemStack> leftOver = p.getInventory().addItem(i);
						if (!leftOver.isEmpty()) {
							for (ItemStack item : leftOver.values()) {
								p.getWorld().dropItemNaturally(p.getLocation(), item);
							}
						}
					}
					p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
				}
				case 3 -> {
					e.setCancelled(true);
					Product prd = PIF.getProduct(PIF.getSelectedSlot());
					if (prd == null) {
						p.sendMessage(ColorUtils.chat(Prefix.RED + " 상품을 먼저 올려주세요!"));
						p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
					} else {
						prd.addPrice(500);
						p.openInventory(PIF.PriceSet());
						p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
					}
				}
				/*
				case 4 -> {
					if (inv.getItem(4) == null) {
						PIF.removeProduct();
					} else {
						Product prd = new Product(inv.getItem(4), 500);
						PIF.setProduct(prd, prd.getPrice());
					}
				}
				 */
				case 4 ->
					// 1틱 뒤에 실행하여 아이템이 슬롯에 들어간 후 데이터를 처리함
					Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(XmasLegacy.class), () -> {
						ItemStack itemInSlot = inv.getItem(4);
						if (itemInSlot == null || itemInSlot.getType() == Material.AIR) {
							PIF.removeProduct();
						} else {
							Product prd = new Product(itemInSlot, 500);
							PIF.setProduct(prd, prd.getPrice());
							p.openInventory(PIF.PriceSet());
						}
					});
				case 5 -> {
					e.setCancelled(true);
					Product prd = PIF.getProduct(PIF.getSelectedSlot());
					if (prd == null) {
						p.sendMessage(ColorUtils.chat(Prefix.RED + " 상품을 먼저 올려주세요!"));
						p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
					} else {
						if (prd.getPrice() < 500) {
							p.sendMessage(ColorUtils.chat(Prefix.RED + " 가격이 너무 낮습니다!"));
							p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
							return;
						}
						prd.removePrice(500);
						p.openInventory(PIF.PriceSet());
						p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
					}
				}
				case 8 -> {
					e.setCancelled(true);
					Product prd = PIF.getProduct(PIF.getSelectedSlot());
					if (prd == null) {
						p.sendMessage(ColorUtils.chat(Prefix.RED + " 상품을 먼저 올려주세요!"));
						p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
					} else {
						e.getInventory().setItem(4, new ItemStack(Material.AIR));
						PIF.setProduct(prd, prd.getPrice());
						p.openInventory(PIF.MerchantShop());
						p.sendMessage(ColorUtils.chat(Prefix.GREEN + " 상품이 등록되었습니다!"));
						p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
					}

				}
				default -> {
					if (slot < 9) e.setCancelled(true);
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
		if (inv.equals(PIF.PriceSet())) {
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
