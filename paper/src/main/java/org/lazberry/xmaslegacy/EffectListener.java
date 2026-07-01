package org.lazberry.xmaslegacy;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.Annotation.Listeners;
import org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.BerserkerSpeedManager;

import java.util.UUID;

@Listeners
public class EffectListener implements Listener {
    private final SkillEffectManager sem;
    private final XmasLegacy plugin;

    public EffectListener() {
        this.plugin = XmasLegacy.getInstance();
        this.sem = SkillEffectManager.INSTANCE;
    }

    @EventHandler
    public void EntityStunListener(EntityMoveEvent e) {
        LivingEntity le = e.getEntity();
        UUID uuid = le.getUniqueId();
        if (sem.stunMap().contains(uuid)) {
            e.setCancelled(true);
            le.sendActionBar(ColorUtils.chat(Alert.YELLOW + " 스턴상태"));
            //TODO Roped effect
        }
    }

    @EventHandler
    public void PlayerStunListener(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        if (sem.stunMap().contains(uuid)) {
            Location from = e.getFrom();
            Location to = e.getTo();

            if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
                Location newTo = from.clone();
                newTo.setYaw(to.getYaw());
                newTo.setPitch(to.getPitch());
                e.setTo(newTo);
            }

            p.sendActionBar(ColorUtils.chat(Alert.YELLOW + " 스턴상태"));
            //TODO Roped effect
        }
    }

	@EventHandler
	public void deStunWhenDead(EntityDeathEvent e) {
		LivingEntity victim = e.getEntity();
		if (sem.isStunned(victim)) sem.deStun(victim.getUniqueId());
	}

	@EventHandler
	public void deStunWhenPlayerDead(PlayerDeathEvent e) {

	}

    @EventHandler
    public void hideHiddenEntities(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.setPose(Pose.STANDING, true);
        sem.getHiddenEntity().forEach(h -> p.hideEntity(plugin, h));
        BerserkerSpeedManager.removeFlatSpeed(p);
    }

    @EventHandler
    public void removeHidePlayer(PlayerQuitEvent e) {
        sem.showEntity(e.getPlayer());
    }

    @EventHandler
    public void StandUp(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        p.setPose(Pose.STANDING, true);
    }
}