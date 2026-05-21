package xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.EconomyManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.PlayerUtils.BagManager;
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopListener implements Listener {
	private final PriestShopManager PSM;
	private final EconomyManager ECM;
    private final ConductableItems CDI;
	private final XmasLegacy plugin;
    private final BagManager BAG;

	public ShopListener() {
		this.PSM = PriestShopManager.getInstance();
		this.ECM = EconomyManager.getInstance();
        this.CDI = ConductableItems.getInstance();
        this.BAG = BagManager.getInstance();
		this.plugin = XmasLegacy.getInstance();
	}

    private boolean isConductableItem(ItemStack item) {
        return item.isSimilar(CDI.DragonPotion()) ||
                item.isSimilar(CDI.HealerPotion()) ||
                item.isSimilar(CDI.ProtectionPotion()) ||
                item.isSimilar(CDI.SpearPotion()) ||
                item.isSimilar(CDI.DeathSave());
    }

    private @Nullable ItemStack checkType(@Nullable ItemStack item) {
        if (item == null) return null;
        if (item.isSimilar(CDI.DragonPotion())) {
            return CDI.DragonPotion();
        } else if (item.isSimilar(CDI.HealerPotion())) {
            return CDI.HealerPotion();
        } else if (item.isSimilar(CDI.ProtectionPotion())) {
            return CDI.ProtectionPotion();
        } else if (item.isSimilar(CDI.SpearPotion())) {
            return CDI.SpearPotion();
        } else if (item.isSimilar(CDI.DeathSave())) {
            return CDI.DeathSave();
        } else {
            return null;
        }
    }

    @EventHandler
    public void StockAddEvent(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (!(e.getInventory().getHolder() instanceof StockInterface)) return;
        if (!(e.getClickedInventory().getHolder() instanceof StockInterface)) return;

		PriestShop shop = PSM.get(p.getUniqueId());
        int slot = e.getRawSlot();
        switch (slot) {
            case 3 -> {
                e.setCancelled(true);
                ItemStack item = e.getInventory().getItem(4);

                if (item == null || item.isEmpty()) return;

                String data = item.getPersistentDataContainer().get(plugin.getNamespacedKey("potion"), PersistentDataType.STRING);
                if (data == null || !isConductableItem(item)) {
                    p.sendMessage(ColorUtils.chat(Alert.RED + " 판매 가능한 아이템이 아닙니다!"));
                    p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
                    return;
                }

                if (isConductableItem(item)) {
                    switch (data) {
                        case "dragon_breath" -> shop.addDragonStock(item.getAmount());
                        case "healing" -> shop.addHealerStock(item.getAmount());
                        case "protection" -> shop.addProtectionStock(item.getAmount());
                        case "spear" -> shop.addSpearStock(item.getAmount());
                        case "death_save" -> shop.addSaveStock(item.getAmount());
                    }

                    p.sendMessage(ColorUtils.chat(Alert.YELLOW + " 아이템이 추가되었습니다! &6수량: " + item.getAmount()));
                    p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);

                    item.setAmount(0);
                }
            }

            case 5 -> {
                e.setCancelled(true);
                if (PSM.getOrCreate(p).isShopEnabled()) {
                    p.sendMessage(ColorUtils.chat(Alert.RED + " 상점이 이미 시작되어 회수할 수 없습니다!"));
                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
                    return;
                }

                List<ItemStack> itemsToReturn = new ArrayList<>();
                if (shop.getDragonStock() > 0) itemsToReturn.add(createReturnItem(CDI.DragonPotion(), shop.getDragonStock()));
                if (shop.getHealerStock() > 0) itemsToReturn.add(createReturnItem(CDI.HealerPotion(), shop.getHealerStock()));
                if (shop.getProtectionStock() > 0) itemsToReturn.add(createReturnItem(CDI.ProtectionPotion(), shop.getProtectionStock()));
                if (shop.getSpearStock() > 0) itemsToReturn.add(createReturnItem(CDI.SpearPotion(), shop.getSpearStock()));
                if (shop.getSaveStock() > 0) itemsToReturn.add(createReturnItem(CDI.DeathSave(), shop.getSaveStock()));

	            shop.setDragonStock(0);
	            shop.setHealerStock(0);
	            shop.setProtectionStock(0);
	            shop.setSpearStock(0);
	            shop.setSaveStock(0);

                for (ItemStack returnItem : itemsToReturn) {
                    Map<Integer, ItemStack> invLeftover = p.getInventory().addItem(returnItem);

                    if (!invLeftover.isEmpty()) {
                        for (ItemStack toBag : invLeftover.values()) {
                            BAG.addItem(p, toBag, toBag.getAmount());
                        }
                    }
                }

                p.sendMessage(ColorUtils.chat(Alert.GREEN + " 모든 재고를 회수했습니다!"));
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            }
            case 6 -> {
				e.setCancelled(true);
                if (PSM.getOrCreate(p).isShopEnabled()) {
                    p.sendMessage(ColorUtils.chat(Alert.RED + " 이미 상점이 시작되었습니다!"));
                    p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                } else {
					if (PSM.getOrCreate(p).getStockCount() == 0) {
						p.sendMessage(ColorUtils.chat(Alert.RED + " 재고가 없습니다!"));
						p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
					} else {
						PSM.getOrCreate(p).enableShop();
						p.sendMessage(ColorUtils.chat(Alert.GREEN + " 상점을 시작했습니다!"));
						p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
						PSM.getOrCreate(p).openShop(p);
					}
                }
            }
        }
    }

    @Contract("_, _ -> new")
    private ItemStack createReturnItem(ItemStack base, int amount) {
        ItemStack clone = base.clone();
        clone.setAmount(amount);
        return clone;
    }

	@EventHandler
	public void onTrade(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player viewer)) return;
		if (!(e.getInventory().getHolder() instanceof ShopInterface gui)) return;

		e.setCancelled(true);

		Player owner = gui.getOwner();
		PriestShop shop = gui.getShop();
		if (owner == null) return;

		if (!owner.isOnline()) {
			viewer.sendMessage(ColorUtils.chat(Alert.RED + " 주인장이 문을 닫았네요!"));
			viewer.closeInventory();
			return;
		}
		int slot = e.getRawSlot();

		switch (slot) {
			case 2 -> processPurchase(viewer, owner, "dragon", Constants.DRAGON_BREATH_PRICE, CDI.DragonPotion());
			case 3 -> processPurchase(viewer, owner, "healer", Constants.HEALER_POTION_PRICE, CDI.HealerPotion());
			case 4 -> processPurchase(viewer, owner, "protection", Constants.PROTECTION_POTION_PRICE, CDI.ProtectionPotion());
			case 5 -> processPurchase(viewer, owner, "spear", Constants.SPEAR_POTION_PRICE, CDI.SpearPotion());
			case 6 -> processPurchase(viewer, owner, "save", Constants.DEATH_SAVER_PRICE, CDI.DeathSave());
		}
	}

	private void processPurchase(Player viewer, Player owner, String type, int price, ItemStack item) {
		int currentStock = switch (type) {
			case "dragon" -> PSM.getOrCreate(owner).getDragonStock();
			case "healer" -> PSM.getOrCreate(owner).getHealerStock();
			case "protection" -> PSM.getOrCreate(owner).getProtectionStock();
			case "spear" -> PSM.getOrCreate(owner).getSpearStock();
			case "save" -> PSM.getOrCreate(owner).getSaveStock();
			default -> 0;
		};

		if (currentStock <= 0) {
			viewer.sendMessage(ColorUtils.chat(Alert.RED + " 재고가 부족합니다!"));
			viewer.playSound(viewer, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			return;
		}

		if (!ECM.transferMoney(viewer.getUniqueId(), owner.getUniqueId(), price)) {
			viewer.sendMessage(ColorUtils.chat(Alert.RED + " 돈이 부족합니다!"));
			viewer.playSound(viewer, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			return;
		}

		switch (type) {
			case "dragon" -> PSM.getOrCreate(owner).setDragonStock(currentStock - 1);
			case "healer" -> PSM.getOrCreate(owner).setHealerStock(currentStock - 1);
			case "protection" -> PSM.getOrCreate(owner).setProtectionStock(currentStock - 1);
			case "spear" -> PSM.getOrCreate(owner).setSpearStock(currentStock - 1);
			case "save" -> PSM.getOrCreate(owner).setSaveStock(currentStock - 1);
		}

		viewer.sendMessage(ColorUtils.chat(Alert.GREEN + " 상품을 구매하였습니다."));
		Map<Integer, ItemStack> leftOver = viewer.getInventory().addItem(item);
		if (!leftOver.isEmpty()) {
			leftOver.values().forEach(s -> BAG.addItem(viewer, s, s.getAmount()));
			viewer.sendMessage(ColorUtils.chat(Alert.YELLOW + " 공간이 부족하여 아이템이 가방으로 이동합니다."));
		}

		viewer.playSound(viewer, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);

		if (PSM.getOrCreate(owner).getStockCount() > 0) {
			viewer.openInventory(new ShopInterface(PSM.getOrCreate(owner), CDI).getInventory());
		} else {
			viewer.closeInventory();
			viewer.sendMessage(ColorUtils.chat(Alert.RED + " 모든 재고가 소진되어 상점이 종료되었습니다."));
		}
	}

	@EventHandler
	public void onTradeDone(InventoryCloseEvent e) {
		if (!(e.getInventory().getHolder() instanceof ShopInterface shop)) return;
		if (shop.getShop().getStockCount() == 0) shop.getShop().disableShop();
	}
}
