package org.lazberry.xmaslegacy.casino.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.bags.BagManager;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.Map;
import java.util.function.Consumer;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class CasinoShopListener implements Listener {
    private final UserManager um;
    private final BagManager bm;
    private final XmasLegacy plugin;

    @Inject
    public CasinoShopListener(UserManager um, BagManager bm, XmasLegacy plugin) {
        this.um = um;
        this.bm = bm;
        this.plugin = plugin;
    }

    private void canBuy(@NotNull Player player, int price, Consumer<Player> accept) {
        User user = um.getUser(player.getUniqueId());

        if (user == null) {
            InfoUtils.error(player, "구매에 실패했습니다.");
            return;
        }
        if (user.getDollars() >= price) {
            user.addDollars(-price);
            accept.accept(player);
        } else InfoUtils.error(player, "잔액이 부족합니다.");
    }

    @EventHandler
    public void buyCasinoItem(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;
        Inventory inv = e.getClickedInventory();

        if (inv == null) return;
        if (inv.getHolder() instanceof CasinoShop shop) {
            e.setCancelled(true);

            int slot = e.getRawSlot();
            if (slot == 1) return;
            int price = slot == 0 ? 5200 : shop.getPriceBySlot(slot);

            if (slot == 0) canBuy(p, price, buyer -> {
                handleItemDrop(buyer, Casino.entranceTicket());
                InfoUtils.info(buyer, "카지노 입장 티켓을 구매하였습니다.");
            });
            else canBuy(p, price, buyer -> {
                ItemStack item = shop.getItemBySlot(slot);
                handleItemDrop(buyer, item);
                InfoUtils.info(buyer, "카지노 코인을 구매하였습니다.");
            });
        }
    }

    private void handleItemDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftOver = player.getInventory().addItem(item);
        if (leftOver.isEmpty()) return;

        leftOver.values().forEach(i -> bm.addItem(player, i));
    }
}
