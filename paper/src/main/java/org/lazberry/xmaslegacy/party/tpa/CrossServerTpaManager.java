package org.lazberry.xmaslegacy.party.tpa;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Registry.Exclude(type = ServerType.LOBBY)
public class CrossServerTpaManager extends AbstractTpaSubscriber implements Initiator {
    private static final Logger log = LoggerFactory.getLogger(CrossServerTpaManager.class);

    public CrossServerTpaManager() {}

    @Override
    public void init() {
        subscribe();
    }

    @Override
    protected void onTpaRequested(UUID senderUuid, String senderName, UUID targetUuid) {
        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null && target.isOnline()) {
            InfoUtils.info(target, "&6{}&f님의 텔레포트 요청이 도착했습니다. &6'/tpaccept'&f로 수락하세요.");
        } else log.error("Tpa target player is not valid at all.");
    }

    @Override
    protected void onCrossTpAccepted(UUID senderUuid, String targetServer, UUID targetUuid) {
        Player sender = Bukkit.getPlayer(senderUuid);
        if (sender == null || !sender.isOnline()) return;

        log.info("Cross-server TPA triggered for sender {} to move to server {}", sender.getName(), targetServer);
    }
}