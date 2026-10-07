package org.lazberry.xmaslegacy.enchant.listener;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.EconomyManager;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.enchant.material.EnchantMaterial;
import org.lazberry.xmaslegacy.enchant.material.shop.EnchantMaterialShop;
import org.lazberry.xmaslegacy.enchant.material.shop.EnchantShopManager;
import org.lazberry.xmaslegacy.enchant.material.shop.ProtectorShop;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class EnchantShopListener implements Listener {
    private final EnchantShopManager shopManager;
    private final EconomyManager economyManager;

    @Inject
    public EnchantShopListener(EnchantShopManager shopManager, EconomyManager economyManager) {
        this.shopManager = shopManager;
        this.economyManager = economyManager;
    }

    @EventHandler
    public void whenClickOnEnchantShop(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        var topInv = e.getInventory();
        if (!(topInv.getHolder() instanceof EnchantMaterialShop shop)) return;

        e.setCancelled(true);

        var clickedInv = e.getClickedInventory();
        if (clickedInv == null || !clickedInv.equals(topInv)) return;

        int slot = e.getSlot();
        switch (slot) {
            case 0 -> {
                p.openInventory(shopManager.getProtectorShop().getInventory());
                p.getWorld().playSound(p, Sound.BLOCK_LEVER_CLICK, 1.0f, 1.0f);
            }
            case 1 -> {}
            default -> {
                int giveAmount = shop.getAmountBySlot(slot);
                int price = shop.getPriceBySlot(slot);
                if (giveAmount == 0) return;

                var uuid = p.getUniqueId();
                var giveItem = EnchantMaterial.PrismFractal().clone();
                giveItem.setAmount(giveAmount);

                var event = new PlayerPurchaseEnchantHelperEvent(p, shop.getInventory(), giveItem, price);
                Bukkit.getPluginManager().callEvent(event);
                if (event.isCancelled()) return;

                if (economyManager.withdraw(uuid, event.getPrice())) {
                    InventoryHelper.giveItemOrKeep(p, giveItem);
                    p.playSound(p, Sound.BLOCK_AMETHYST_BLOCK_FALL, 1.0f, 1.0f);
                } else {
                    InfoUtils.error(p, "잔액이 부족합니다.");
                }
            }
        }
    }

    @EventHandler
    public void whenClickOnProtectorShop(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        var topInv = e.getInventory();
        if (!(topInv.getHolder() instanceof ProtectorShop shop)) return;

        e.setCancelled(true);

        var clickedInv = e.getClickedInventory();
        if (clickedInv == null || !clickedInv.equals(topInv)) return;

        int slot = e.getSlot();
        var uuid = p.getUniqueId();

        if (slot == 1 || slot == 4 || slot == 7) {
            int price = shop.getPriceBySlot(slot);
            if (price == 0) return;
            ItemStack giveItem = switch (slot) {
                case 1 -> EnchantMaterial.InfoBook();
                case 4 -> EnchantMaterial.DowngradeProtector();
                case 7 -> EnchantMaterial.BreakProtector();
                default -> null;
            };

            var event = new PlayerPurchaseEnchantHelperEvent(p, shop.getInventory(), giveItem, price);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return;

            if (economyManager.withdraw(uuid, event.getPrice())) {
                InventoryHelper.giveItemOrKeep(p, giveItem);
                p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            } else {
                InfoUtils.error(p, "잔액이 부족합니다.");
            }
        }
    }
}
