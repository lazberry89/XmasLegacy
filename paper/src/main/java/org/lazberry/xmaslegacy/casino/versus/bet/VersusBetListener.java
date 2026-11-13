package org.lazberry.xmaslegacy.casino.versus.bet;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.bags.BagManager;
import org.lazberry.xmaslegacy.casino.versus.VersusManager;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

@Listeners
@Components.Include(type = ServerType.MAIN)
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

        // Any early return below leaves the coins in the slot; they go back to the player when the screen closes.
        if (slot == 3 || slot == 5) {
            // The screen can be kept open past the deadline, so check again at click time.
            if (!vm.isCanBet()) {
                InfoUtils.error(p, "베팅 세션이 종료되었습니다.");
                return;
            }
            // A second bet would overwrite the first record and its coins would be gone.
            if (vbm.getBetRecord(p.getUniqueId()).isPresent()) {
                InfoUtils.error(p, "이미 베팅하셨습니다.");
                return;
            }
            if (amount > vm.getMaxBetAmount()) {
                InfoUtils.error(p, "베팅 최대 갯수는 &6{}개&f 입니다.", vm.getMaxBetAmount());
                return;
            }
            if (amount == 0) {
                InfoUtils.error(p, "빈칸에 베팅할 카지노 코인을 올려주세요!");
                return;
            }
            var selected = slot == 3 ? bet.getFighter1() : bet.getFighter2();
            vbm.bet(p, selected, amount);
            var player = Bukkit.getPlayer(selected);
            if (player != null) InfoUtils.info(p, "&6{}&f님에게 &6{}&f코인을 베팅했습니다.", player.getName(), amount);
            p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
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
