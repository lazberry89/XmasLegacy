package org.lazberry.xmaslegacy.teleporter.listener;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.teleporter.logic.TeleporterManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.Optional;

@Registry.Include(type = ServerType.GLOBAL)
public class TeleporterListener implements Listener {
    private final TeleporterManager tm;

    @Inject
    public TeleporterListener(TeleporterManager tm) {
        this.tm = tm;
    }

    @EventHandler
    public void teleportPlayerWhenSteppingOn(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Location to = e.getTo();
        Location from = e.getFrom();
        if (!e.hasChangedBlock()) return;
        if (tm.canTeleport(from, to)) {
            if (tm.isCooldown(player.getUniqueId())) {
                Vector moveDirection = to.toVector().subtract(from.toVector()).normalize();

                Vector bounceVelocity = moveDirection.multiply(-1.2).setY(0.25);

                player.setVelocity(bounceVelocity);
                player.playSound(player.getLocation(), Sound.ENTITY_BAT_TAKEOFF, 0.8f, 0.8f);
                InfoUtils.warn(player, "재사용 대기시간 중에는 포탈을 이용할 수 없습니다!");
                return;
            }
            Optional<Location> optional = tm.getDestination(to);
            if (optional.isEmpty()) {
                InfoUtils.error(player, "도착지의 포탈이 파괴되었거나 더이상 유효하지 않습니다.");
                return;
            }
            Location destination = optional.get();
            player.teleport(destination);
            tm.applyCooldown(player.getUniqueId());
            player.playSound(destination, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }
    }
}
