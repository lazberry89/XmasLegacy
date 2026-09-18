package org.lazberry.xmaslegacy.casino.versus.bet;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.bags.BagManager;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class VersusBetListener implements Listener {
    private final VersusBetManager vbm;
    private final BagManager bm;
    private final XmasLegacy plugin;

    @Inject
    public VersusBetListener(VersusBetManager vbm, BagManager bm, XmasLegacy plugin) {
        this.vbm = vbm;
        this.bm = bm;
        this.plugin = plugin;
    }

    @EventHandler
    public void betInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player p)) return;

        var inv = e.getClickedInventory();
        if (inv == null) return;
        if (!(inv.getHolder() instanceof VersusBetInterface bet)) return;

        int amount = bet.getBetAmount();
        int slot = e.getRawSlot();

        e.setCancelled(slot != 4);

        if (slot == 3 || slot == 5) {
            if (amount == 0) {
                InfoUtils.error(p, "빈칸에 베팅할 카지노 코인을 올려주세요!");
                return;
            }
            if (slot == 3) {
                vbm.bet(p, bet.getFighter1(), amount);
                var player = Bukkit.getPlayer(bet.getFighter1());
                if (player != null) InfoUtils.info(p, "&6{}&f님에게 &6{}&f코인을 베팅했습니다.", player.getName());
                p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
            else {
                vbm.bet(p, bet.getFighter2(), amount);
                var player = Bukkit.getPlayer(bet.getFighter2());
                if (player != null) InfoUtils.info(p, "&6{}&f님에게 &6{}&f코인을 베팅했습니다.", player.getName());
                p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
            bet.clearBettingSlot();
            Bukkit.getScheduler().runTask(plugin, () -> p.closeInventory(InventoryCloseEvent.Reason.PLUGIN));
        }
    }

    @EventHandler
    public void givebackCoinWhenClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        if (!(e.getInventory().getHolder() instanceof VersusBetInterface bet)) return;
        if (e.getReason() == InventoryCloseEvent.Reason.PLUGIN) return;

        int amount = bet.getBetAmount();
        if (amount == 0) return;

        var remains = p.getInventory().addItem(Casino.coin(amount));
        if (!remains.isEmpty()) bm.addAll(p, remains.values());
        InfoUtils.warn(p, "코인이 회수되었습니다.");
    }
}
