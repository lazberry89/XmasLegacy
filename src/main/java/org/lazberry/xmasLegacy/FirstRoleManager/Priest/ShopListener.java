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
import org.lazberry.xmasLegacy.Prefix;
import org.lazberry.xmasLegacy.User.UserManager;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class ShopListener implements Listener {
	private final PriestShop PSP;
	private final UserManager UM;
	private final EconomyManager ECM;
    private final ConductableItems CDI;
	private final XmasLegacy plugin;

	public ShopListener(PriestShop PSP, UserManager UM, EconomyManager ECM, ConductableItems CDI, XmasLegacy plugin) {
		this.PSP = PSP;
		this.UM = UM;
		this.ECM = ECM;
        this.CDI = CDI;
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
        ItemStack item = e.getInventory().getItem(4);
        if (item == null) return;
        String data = item.getPersistentDataContainer().get(plugin.getNamespacedKey("potion"), PersistentDataType.STRING);

        if (data == null) return;
        switch (slot) {
            case 3 -> {
                e.setCancelled(true);
                if (item.isEmpty()) return;
                if (isConductableItem(item)) {
                    switch (data) {
                        case "dragon_breath" -> {
                            PSP.addDragonStock(item.getAmount());
                            p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 아이템이 추가되었습니다! &6수량: " + item.getAmount()));
                            p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                        }
                        case "healing" -> {
                            PSP.addHealerStock(item.getAmount());
                            p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 아이템이 추가되었습니다! &6수량: " + item.getAmount()));
                            p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                        }
                        case "protection" -> {
                            PSP.addProtectionStock(item.getAmount());
                            p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 아이템이 추가되었습니다! &6수량: " + item.getAmount()));
                            p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                        }
                        case "spear" -> {
                            PSP.addSpearStock(item.getAmount());
                            p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 아이템이 추가되었습니다! &6수량: " + item.getAmount()));
                            p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                        }
                        case "death_save" -> {
                            PSP.addSaveStock(item.getAmount());
                            p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 아이템이 추가되었습니다! &6수량: " + item.getAmount()));
                            p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                        }
                        default -> {
                            p.sendMessage(ColorUtils.chat(Prefix.RED + " 판매 가능한 아이템이 아닙니다!"));
                            p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
                        }
                    }
                } else {
                    p.sendMessage(ColorUtils.chat(Prefix.RED + " 판매 가능한 아이템이 아닙니다!"));
                    p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
                }
            }
            case 5 -> {
                if (PSP.isShopEnabled()) {
                    p.sendMessage(ColorUtils.chat(Prefix.RED + " 상점이 이미 시작되어 회수할 수 없습니다!"));
                    p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.5f, 1.0f);
                } else {
                    int dragon = PSP.getDragonStock();
                    int healing = PSP.getHealerStock();
                    int protection = PSP.getProtectionStock();
                    int spear = PSP.getSpearStock();
                    int death = PSP.getSaveStock();

                    PSP.setDragonStock(0);
                    PSP.setHealerStock(0);
                    PSP.setProtectionStock(0);
                    PSP.setSpearStock(0);
                    PSP.setSaveStock(0);

                    ItemStack dragonPotion = CDI.DragonPotion();
                    dragonPotion.setAmount(dragon);
                    ItemStack healerPotion = CDI.HealerPotion();
                    healerPotion.setAmount(healing);
                    ItemStack protectionPotion = CDI.ProtectionPotion();
                    protectionPotion.setAmount(protection);
                    ItemStack spearPotion = CDI.SpearPotion();
                    spearPotion.setAmount(spear);
                    ItemStack saver = CDI.DeathSave();
                    saver.setAmount(death);

                    HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(dragonPotion);
                }
            }
        }
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
		ItemStack money = ingredients.getFirst();
		NamespacedKey key = plugin.getNamespacedKey("money");

		Integer mc = money.getPersistentDataContainer().get(key, PersistentDataType.INTEGER);
		if (mc == null) return;
		int count = mc * money.getAmount();

		if (count <= 0) return;
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
