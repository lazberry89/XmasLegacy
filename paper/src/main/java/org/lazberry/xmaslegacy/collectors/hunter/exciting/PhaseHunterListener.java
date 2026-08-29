package org.lazberry.xmaslegacy.collectors.hunter.exciting;

import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.collectors.drop.PlayerBreakContainerEvent;
import org.lazberry.xmaslegacy.collectors.drop.PlayerHitContainerEvent;
import org.lazberry.xmaslegacy.collectors.game.CollectorsManager;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.collectors.game.Session;
import org.lazberry.xmaslegacy.collectors.hunter.HunterRepository;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.TitleUtil;

import java.util.Collection;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class PhaseHunterListener implements Listener {
    private final PhaseHunterHandler handler;

    @Inject
    public PhaseHunterListener(PhaseHunterHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void dismissWhenHunterIsPhaseMode(EntityTargetLivingEntityEvent e) {
        Entity targeter = e.getEntity();
        if (!(targeter instanceof WitherSkeleton skeleton)) return;
        if (!(e.getTarget() instanceof Player)) return;

        if (PhaseHunter.isEntity(skeleton) && PhaseHunter.isPhase(skeleton)) {
            e.setCancelled(true);
            e.setTarget(null);
        }
    }

    @EventHandler
    public void moveToPlayerWhenPlayerHitContainer(PlayerHitContainerEvent e) {
        Player player = e.getPlayer();
        e.getAffectedEntities().forEach(h -> {
            if (h instanceof Mob mob) mob.getPathfinder().moveTo(player, 1.0);
        });
    }

    @EventHandler
    public void triggerHuntingModeWhenPlayerBreakContainer(PlayerBreakContainerEvent e) {
        Player player = e.getPlayer();
        Collection<LivingEntity> affectedEntities = e.getAffectedEntities(6);
        if (affectedEntities.isEmpty()) return;

        affectedEntities.forEach(h -> {
            if (h instanceof WitherSkeleton skeleton) {
                handler.triggerAggro(skeleton);
                skeleton.setTarget(player);
            }
        });
        player.sendActionBar(ColorUtils.chat("&4⚠️ 발각됨"));
        player.playSound(player, Sound.ENTITY_GHAST_SCREAM, 1.0f, 1.2f);
    }
}
