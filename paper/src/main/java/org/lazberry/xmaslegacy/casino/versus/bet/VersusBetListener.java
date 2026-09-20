package org.lazberry.xmaslegacy.casino.versus.bet;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.bags.BagManager;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.casino.versus.VersusManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class VersusBetListener implements Listener {
    private final VersusBetManager vbm;
    private final VersusManager vm;
    private final BagManager bm;
    private final XmasLegacy plugin;

    @Inject
    public VersusBetListener(VersusBetManager vbm, VersusManager vm, BagManager bm, XmasLegacy plugin) {
        this.vbm = vbm;
        this.vm = vm;
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
            if (amount > vm.getMaxBetAmount()) {
                InfoUtils.error(p, "베팅 최대 갯수는 &6{}개&f 입니다.", vm.getMaxBetAmount());
                return;
            }
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

        ItemStack item = bet.getInventory().getItem(4);
        if (item == null || item.getType().isAir()) return;

        var remains = p.getInventory().addItem(item);
        if (!remains.isEmpty()) bm.addAll(p, remains.values());
        bet.clearBettingSlot();
        InfoUtils.warn(p, "올려둔 아이템이 회수되었습니다.");
    }
}
