package org.lazberry.xmaslegacy.collectors.hunter.horror;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.collectors.drop.PlayerBreakContainerEvent;
import org.lazberry.xmaslegacy.collectors.drop.PlayerHitContainerEvent;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.Vignette;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class SilenceListener implements Listener {

    @EventHandler
    public void clearVignetteOnPlayer(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Vignette.clearVignette(player);
        clearTargetIfChased(player);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        clearTargetIfChased(e.getEntity());
    }

    private void clearTargetIfChased(Player target) {
        for (World world : Bukkit.getWorlds()) {
            for (Warden warden : world.getEntitiesByClass(Warden.class)) {
                if (!Silence.isEntity(warden) || !Silence.isChasing(warden)) continue;

                LivingEntity currentTarget = warden.getTarget();
                if (target.equals(currentTarget)) {
                    Silence.setChasing(warden, false);
                    warden.setAnger(target, 0);
                    warden.setTarget(null);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTargetChange(EntityTargetLivingEntityEvent e) {
        if (!(e.getEntity() instanceof Warden warden) || !Silence.isEntity(warden)) return;

        LivingEntity newTarget = e.getTarget();

        if (newTarget == null || !newTarget.isValid()) {
            Silence.setChasing(warden, false);
            LivingEntity prevTarget = warden.getTarget();
            if (prevTarget != null) {
                warden.setAnger(prevTarget, 0);
            }
            return;
        }

        if (Silence.isChasing(warden)) {
            LivingEntity currentTarget = warden.getTarget();
            if (currentTarget != null && currentTarget.isValid() && !currentTarget.equals(newTarget)) {
                e.setCancelled(true);
                warden.setAnger(currentTarget, 150);
            }
            return;
        }

        if (newTarget instanceof Player player) {
            e.setCancelled(true);
            warden.setAnger(player, 0);
            warden.setTarget(null);
        }
    }

    @EventHandler
    public void onHitContainer(PlayerHitContainerEvent e) {
        Location targetLoc = e.getLocation();

        for (LivingEntity entity : e.getAffectedEntities()) {
            if (!(entity instanceof Mob mob) || !Silence.isEntity(mob)) continue;

            if (!Silence.isFurious(mob) && !Silence.isChasing(mob)) {
                // 단순 pathfinder 호출 + 워든에게 진동(Vibration) 시뮬레이션
                mob.getPathfinder().moveTo(targetLoc, 1.2D);
            }
        }
    }

    @EventHandler
    public void onBreakContainer(PlayerBreakContainerEvent e) {
        Player player = e.getPlayer();

        for (LivingEntity entity : e.getAffectedEntities(6)) {
            if (!(entity instanceof Warden warden) || !Silence.isEntity(warden)) continue;

            if (!Silence.isChasing(warden)) {
                startChasing(warden, player);
                World world = warden.getWorld();
                world.playSound(warden.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.0f, 1.0f);
            }
        }
    }

    /**
     * 3. 분노(Furious) 상태일 때 유저가 블록을 이동하면 즉시 추격
     */
    @EventHandler
    public void onMoveDuringRage(PlayerMoveEvent e) {
        if (!e.hasChangedBlock()) return;

        Player player = e.getPlayer();

        for (Entity nearby : player.getNearbyEntities(15, 15, 15)) {
            if (!(nearby instanceof Warden warden) || !Silence.isEntity(warden)) continue;

            if (Silence.isFurious(warden) && !Silence.isChasing(warden)) {
                startChasing(warden, player);
                World world = warden.getWorld();
                world.playSound(warden.getLocation(), Sound.ENTITY_WARDEN_AGITATED, 1.0f, 0.8f);
            }
        }
    }


    private void startChasing(Warden warden, Player target) {
        Silence.setChasing(warden, true);
        warden.setTarget(target);
        warden.setAnger(target, 150);
    }
}