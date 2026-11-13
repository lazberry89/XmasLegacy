package org.lazberry.xmaslegacy.casino.versus;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.casino.versus.event.VersusResetEvent;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.OptionalUtils;
import org.lazberry.xmaslegacy.utils.ServerTransfer;
import org.lazberry.xmaslegacy.utils.TitleUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Listeners
@Components.Include(type = ServerType.MAIN)
public class VersusGameListener implements Listener {
    private final VersusManager vm;
    private final XmasLegacy plugin;
    private final Set<UUID> spectatingLosers = new HashSet<>();

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
            if (!f.isRunning() || !f.isFighter(uuid)) return;

            // An arena death must not spill the fighter's inventory and levels onto the floor.
            e.setKeepInventory(true);
            e.getDrops().clear();
            e.setKeepLevel(true);
            e.setDroppedExp(0);

            vm.reset(uuid.equals(f.getBlueFighter()) ? GameResult.RED : GameResult.BLUE);
        });
    }

    @EventHandler
    public void handleFighterQuit(PlayerQuitEvent e) {
        var p = e.getPlayer();
        var uuid = p.getUniqueId();

        vm.whenFieldExists(f -> {
            if (f.isFighter(uuid)) {
                // Mid-fight the quitter forfeits; before the fight the match is cancelled and stakes go back.
                if (f.isRunning()) vm.reset(uuid.equals(f.getBlueFighter()) ? GameResult.RED : GameResult.BLUE);
                else vm.reset(GameResult.LEAVE);
            }

            // Otherwise the loser would log back in as a spectator inside the arena.
            if (spectatingLosers.remove(uuid)) {
                p.setGameMode(GameMode.SURVIVAL);
                p.teleport(f.getEntrance());
            }
        });
    }

    @EventHandler
    public void versusResultEffectsAndClear(VersusResetEvent e) {
        var winner = e.getWinner();
        var loser = e.getLoser();

        vm.whenFieldExists(f -> {
            if (winner == null || loser == null) {
                // Cancelled before a winner was decided: both stakes go back.
                for (UUID fighter : new UUID[]{e.getBlueTeamFighter(), e.getRedTeamFighter()}) {
                    if (fighter == null) continue;
                    OptionalUtils.ifNotNull(Bukkit.getPlayer(fighter), p -> {
                        Casino.giveCoin(p, vm.getJoinCoinAmount());
                        p.teleport(f.getEntrance());
                        InfoUtils.warn(p, "경기가 취소되어 참가비가 반환되었습니다.");
                    });
                }
                // shortcut: a fixed pause outlasts the cancelled match's leftover timers (about 5.5s);
                // switch to a per-match id if the countdown lengths ever become configurable.
                f.setCanStart(false);
                Bukkit.getScheduler().runTaskLater(plugin, () -> f.setCanStart(true), 20 * 8L);
                return;
            }

            f.setCanStart(false);
            var titleWin = TitleUtil.create("&6&l승리!", "5초뒤 복귀합니다.");
            var titleLose = TitleUtil.create("&c&l패배", "2초뒤 복귀합니다.");

            OptionalUtils.ifNotNull(Bukkit.getPlayer(loser), l -> {
                l.setGameMode(GameMode.SPECTATOR);
                spectatingLosers.add(loser);
                l.showTitle(titleLose);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (!spectatingLosers.remove(loser)) return; // quit meanwhile: handled in handleFighterQuit
                    OptionalUtils.ifNotNull(Bukkit.getPlayer(loser), rl -> {
                        rl.setGameMode(GameMode.SURVIVAL);
                        ServerTransfer.dramaticTeleport(rl, f.getEntrance());
                        InfoUtils.info(rl, "복귀했습니다.");
                    });
                }, 20 * 2L);
            });

            OptionalUtils.ifNotNullOrElse(Bukkit.getPlayer(winner), w -> {
                w.showTitle(titleWin);
                Casino.giveCoin(w, vm.getJoinCoinAmount() * 2);
            }, () -> {});

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                OptionalUtils.ifNotNull(Bukkit.getPlayer(winner), rw -> {
                    ServerTransfer.dramaticTeleport(rw, f.getEntrance());
                    InfoUtils.info(rw, "복귀하였습니다.");
                });
                f.setCanStart(true);
            }, 20 * 5L);
        });
    }
}
