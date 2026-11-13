package org.lazberry.xmaslegacy.party.tpa;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.UUID;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class CrossServerTpaManager extends AbstractTpaSubscriber implements Initiator {
    private final XmasLegacy plugin;

    @Inject
    public CrossServerTpaManager(XmasLegacy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void init() {
        subscribe();
    }

    @Override
    public void close() {
        unsubscribeSubscriber();
    }

    @Override
    protected void onTpaRequested(UUID senderUuid, String senderName, UUID targetUuid) {
        // Called on the Redis subscriber thread: hop to the main thread before touching Bukkit.
        Bukkit.getScheduler().runTask(plugin, () -> {
            // Every paper server gets the signal; only the one holding the target reacts.
            Player target = Bukkit.getPlayer(targetUuid);
            if (target == null) return;
            InfoUtils.info(target, "&6{}&f님의 텔레포트 요청이 도착했습니다. &6'/tpaccept'&f로 수락하세요.", senderName);
        });
    }

    /** The proxy moves the requester (TpaActualTeleportManager); paper has nothing to do here. */
    @Override
    protected void onCrossTpAccepted(UUID senderUuid, String targetServer, UUID targetUuid) {
        log.info("Cross-server TPA: {} is being moved to server {}", senderUuid, targetServer);
    }
}
