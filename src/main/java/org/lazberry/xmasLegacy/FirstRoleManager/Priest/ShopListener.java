package org.lazberry.xmasLegacy.FirstRoleManager.Priest;

import io.papermc.paper.event.player.PlayerTradeEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Merchant;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmasLegacy.Economy.EconomyManager;
import org.lazberry.xmasLegacy.PlayerUtils.BagManager;
import org.lazberry.xmasLegacy.Prefix;
import org.lazberry.xmasLegacy.User.UserManager;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ClassCanBeRecord")
public class ShopListener implements Listener {
	private final PriestShop PSP;
	private final UserManager UM;
	private final EconomyManager ECM;
    private final ConductableItems CDI;
	private final XmasLegacy plugin;
    private final BagManager BAG;

	public ShopListener(PriestShop PSP, UserManager UM, EconomyManager ECM, ConductableItems CDI, BagManager BAG, XmasLegacy plugin) {
		this.PSP = PSP;
		this.UM = UM;
		this.ECM = ECM;
        this.CDI = CDI;
        this.BAG = BAG;
		this.plugin = plugin;
	}

    private boolean isConductableItem(ItemStack item) {
        return item.isSimilar(CDI.DragonPotion()) ||
                item.isSimilar(CDI.HealerPotion()) ||
                item.isSimilar(CDI.ProtectionPotion()) ||
                item.isSimilar(CDI.SpearPotion()) ||
                item.isSimilar(CDI.DeathSave());
    }
    @Contract("null -> null; !null -> !null")
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
        if (!(e.getInventory().getHolder() instanceof ShopInterface)) return;
        if (!(e.getClickedInventory().getHolder() instanceof ShopInterface)) return;

        int slot = e.getRawSlot();

        switch (slot) {
            case 3 -> {
                e.setCancelled(true);
                ItemStack item = e.getInventory().getItem(4);

                if (item == null || item.isEmpty()) return;

                String data = item.getPersistentDataContainer().get(plugin.getNamespacedKey("potion"), PersistentDataType.STRING);
                if (data == null || !isConductableItem(item)) {
                    p.sendMessage(ColorUtils.chat(Prefix.RED + " 판매 가능한 아이템이 아닙니다!"));
                    p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
                    return;
                }

                if (isConductableItem(item)) {
                    switch (data) {
                        case "dragon_breath" -> PSP.addDragonStock(item.getAmount());
                        case "healing" -> PSP.addHealerStock(item.getAmount());
                        case "protection" -> PSP.addProtectionStock(item.getAmount());
                        case "spear" -> PSP.addSpearStock(item.getAmount());
                        case "death_save" -> PSP.addSaveStock(item.getAmount());
                    }

                    p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 아이템이 추가되었습니다! &6수량: " + item.getAmount()));
                    p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);

                    item.setAmount(0);
                }
            }

            case 5 -> {
                e.setCancelled(true);
                if (PSP.isShopEnabled()) {
                    p.sendMessage(ColorUtils.chat(Prefix.RED + " 상점이 이미 시작되어 회수할 수 없습니다!"));
                    p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
                    return;
                }

                List<ItemStack> itemsToReturn = new ArrayList<>();
                if (PSP.getDragonStock() > 0) itemsToReturn.add(createReturnItem(CDI.DragonPotion(), PSP.getDragonStock()));
                if (PSP.getHealerStock() > 0) itemsToReturn.add(createReturnItem(CDI.HealerPotion(), PSP.getHealerStock()));
                if (PSP.getProtectionStock() > 0) itemsToReturn.add(createReturnItem(CDI.ProtectionPotion(), PSP.getProtectionStock()));
                if (PSP.getSpearStock() > 0) itemsToReturn.add(createReturnItem(CDI.SpearPotion(), PSP.getSpearStock()));
                if (PSP.getSaveStock() > 0) itemsToReturn.add(createReturnItem(CDI.DeathSave(), PSP.getSaveStock()));

                PSP.setDragonStock(0);
                PSP.setHealerStock(0);
                PSP.setProtectionStock(0);
                PSP.setSpearStock(0);
                PSP.setSaveStock(0);

                for (ItemStack returnItem : itemsToReturn) {
                    Map<Integer, ItemStack> invLeftover = p.getInventory().addItem(returnItem);

                    if (!invLeftover.isEmpty()) {
                        for (ItemStack toBag : invLeftover.values()) {
                            List<ItemStack> leftOver = BAG.addItem(p, toBag, toBag.getAmount());
                        }
                    }
                }

                p.sendMessage(ColorUtils.chat(Prefix.GREEN + " 모든 재고를 회수했습니다!"));
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
            }
            case 6 -> {
                if (PSP.isShopEnabled()) {
                    p.sendMessage(ColorUtils.chat(Prefix.RED + " 이미 상점이 시작되었습니다!"));
                    p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                } else {
                    PSP.enableShop();
                    p.sendMessage(ColorUtils.chat(Prefix.GREEN + " 상점을 시작했습니다!"));
                    p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    PSP.openShop(p, p);
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
	public void onTrade(PlayerTradeEvent e) {
		Player viewer = e.getPlayer();
		Player owner = PSP.getOwner(e.getMerchant());
        if (owner == null) return;
		if (!owner.isOnline()) {
			viewer.sendMessage(ColorUtils.chat(Prefix.RED + " 주인장이 문을 닫았네요!"));
			e.setCancelled(true);
			return;
		}
		List<ItemStack> ingredients = e.getTrade().getIngredients();
        ItemStack purchase = checkType(e.getTrade().getResult());
        ItemStack result = e.getTrade().getResult();
		ItemStack money = ingredients.getFirst();
		NamespacedKey key = plugin.getNamespacedKey("money");

		Integer mc = money.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
		if (mc == null) return;
		int count = mc * money.getAmount();
        int boughtAmount = result.getAmount();

		if (count <= 0) return;
        if (purchase.isSimilar(CDI.DragonPotion())) {
            PSP.addDragonStock(-boughtAmount);
        } else if (purchase.isSimilar(CDI.HealerPotion())) {
            PSP.addHealerStock(-boughtAmount);
        } else if (purchase.isSimilar(CDI.SpearPotion())) {
            PSP.addSpearStock(-boughtAmount);
        } else if (purchase.isSimilar(CDI.DeathSave())) {
            PSP.addSaveStock(-boughtAmount);
        } else if (purchase.isSimilar(CDI.ProtectionPotion())) {
            PSP.addProtectionStock(-boughtAmount);
        }
		ECM.deposit(owner, count);
        viewer.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 상품을 구매했습니다!"));
		owner.playSound(owner, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
	}

    @EventHandler
    public void onTradeDone(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        if (!(e.getInventory() instanceof MerchantInventory mi)) return;
        Merchant merchant = mi.getMerchant();
        if (PSP.getOwner(merchant) == null) return;
        PSP.removeShop(merchant);
    }
}
