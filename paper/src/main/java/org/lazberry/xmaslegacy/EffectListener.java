package org.lazberry.xmaslegacy;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Listeners;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.Utils.StunUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Lang;

import java.util.UUID;

@Inject
@Listeners
public class EffectListener implements Listener {
    private @NotNull SkillEffectManager sem;
    private @NotNull XmasLegacy plugin;

    public EffectListener() {}

    @EventHandler
    public void removeDebuffIfImmune(PlayerMoveEvent e) {
        if (!e.hasChangedPosition()) return;
        var player = e.getPlayer();
        if (sem.isImmuneToDebuff(player.getUniqueId())) SkillEffectManager.clearDebuffs(player);
    }

    @EventHandler
    public void ImmuneToKnockback(EntityKnockbackEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        if (sem.isImmuneToKnockback(uuid)) e.setCancelled(true);
    }

    @EventHandler
    public void immuneVelocity(PlayerVelocityEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        if (sem.isImmuneToKnockback(uuid)) e.setCancelled(true);
    }

    @EventHandler
    public void EntityStunListener(EntityMoveEvent e) {
        LivingEntity le = e.getEntity();
        UUID uuid = le.getUniqueId();
        if (StunUtils.isStunned(uuid)) {
            e.setCancelled(true);
            le.sendActionBar(StunUtils.reasonIndicator(uuid, Lang.KOREAN));
            //TODO Roped effect
        }
    }

    @EventHandler
    public void PlayerStunListener(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        if (StunUtils.isStunned(uuid)) {
            Location from = e.getFrom();
            Location to = e.getTo();

            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                Location newTo = from.clone();
                newTo.setYaw(to.getYaw());
                newTo.setPitch(to.getPitch());
                e.setTo(newTo);
            }

            p.sendActionBar(StunUtils.reasonIndicator(uuid, Lang.KOREAN));
            //TODO Roped effect
        }
    }

	@EventHandler
	public void deStunWhenDead(EntityDeathEvent e) {
		LivingEntity victim = e.getEntity();
        var uuid = victim.getUniqueId();
		if (StunUtils.isStunned(uuid)) StunUtils.release(uuid);
	}

	@EventHandler
	public void deStunWhenPlayerDead(PlayerDeathEvent e) {
        Player victim = e.getPlayer();
        var uuid = victim.getUniqueId();
        if (StunUtils.isStunned(victim.getUniqueId())) StunUtils.release(uuid);
	}

    @EventHandler
    public void hideHiddenEntities(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.setPose(Pose.STANDING, true);
        sem.getHiddenEntity().forEach(h -> p.hideEntity(plugin, h));
    }

    @EventHandler
    public void removeHidePlayer(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        var uuid = p.getUniqueId();
        sem.showEntity(p);
        if (StunUtils.isStunned(uuid)) StunUtils.release(uuid);
        GlowUtils.clearGlow(p);
    }

    @EventHandler
    public void StandUp(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        p.setPose(Pose.STANDING, true);
    }
}