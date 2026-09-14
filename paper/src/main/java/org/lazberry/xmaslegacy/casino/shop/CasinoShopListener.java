package org.lazberry.xmaslegacy.casino.shop;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.function.Consumer;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class CasinoShopListener implements Listener {
    private final UserManager um;
    private final XmasLegacy plugin;

    @Inject
    public CasinoShopListener(UserManager um, XmasLegacy plugin) {
        this.um = um;
        this.plugin = plugin;
    }

    private void canBuy(@NotNull Player player, int price, Consumer<Player> accept) {
        Runnable fail = () -> InfoUtils.error(player, "구매에 실패했습니다.");
        Runnable notEnough = () -> InfoUtils.error(player, "잔액이 부족합니다.");
        User user = um.getUser(player.getUniqueId());

        if (user == null) {
            fail.run();
            return;
        }
        if (user.getDollars() > price) {
            user.addDollars(-price);
            accept.accept(player);
        }
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
            int price = switch (slot) {
                case 0 ->
            }
        }
    }
}
