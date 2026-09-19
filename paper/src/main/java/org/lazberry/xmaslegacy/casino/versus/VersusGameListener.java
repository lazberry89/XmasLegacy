package org.lazberry.xmaslegacy.casino.versus;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.versus.event.VersusResetEvent;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.OptionalUtils;
import org.lazberry.xmaslegacy.utils.ServerTransfer;
import org.lazberry.xmaslegacy.utils.TitleUtil;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class VersusGameListener implements Listener {
    private final VersusManager vm;
    private final XmasLegacy plugin;

    @Inject
    public VersusGameListener(VersusManager vm, XmasLegacy plugin) {
        this.vm = vm;
        this.plugin = plugin;
    }

    @EventHandler
    public void invalidateDamageByNonFighter(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        var damager = e.getDamager().getUniqueId();
        var uuid = victim.getUniqueId();
        var f = vm.getField();

        f.ifPresent(field -> e.setCancelled(!field.isFighter(uuid) || !field.isFighter(damager)));
    }

    @EventHandler
    public void createResultWhenDead(PlayerDeathEvent e) {
        var p = e.getPlayer();
        var uuid = p.getUniqueId();

        vm.whenFieldExists(f -> {
            if (!f.isRunning()) return;

            if (uuid.equals(f.getBlueFighter())) {
                vm.reset(GameResult.RED);
            } else if (uuid.equals(f.getRedFighter())) {
                vm.reset(GameResult.BLUE);
            }
        });
    }

    @EventHandler
    public void versusResultEffectsAndClear(VersusResetEvent e) {
        var result = e.getResult();

        var winner = e.getWinner();
        var loser = e.getLoser();

        vm.whenFieldExists(f -> {
            if (winner == null || loser == null) return;
            var titleWin = TitleUtil.create("&6&l승리!", "5초뒤 복귀합니다.");
            var titleLose = TitleUtil.create("&c&l패배", "5초뒤 복귀합니다.");

			OptionalUtils.ifNotNull(Bukkit.getPlayer(winner), w -> {

			});
			OptionalUtils.ifNotNull(Bukkit.getPlayer(loser), r -> {});
        });
    }
}
