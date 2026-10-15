package org.lazberry.xmaslegacy.party.tpa;

import lombok.AccessLevel;
import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.JedisService;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

@Slf4j(access = AccessLevel.PROTECTED)
public abstract class AbstractTpaSubscriber extends JedisPubSub {
    private static final String CHANNEL = "xmas:tpa-channel";

    @Override
    public void onMessage(String channel, String message) {
        if (!CHANNEL.equals(channel)) return;

        String[] parts = message.split(":", 4);
        if (parts.length < 2) return;

        String type = parts[0];

        if ("REQUEST".equals(type) && parts.length >= 4) {
            UUID senderUuid = UUID.fromString(parts[1]);
            String senderName = parts[2];
            UUID targetUuid = UUID.fromString(parts[3]);
            onTpaRequested(senderUuid, senderName, targetUuid);
        }
        else if ("CROSS_TP".equals(type) && parts.length >= 4) {
            UUID senderUuid = UUID.fromString(parts[1]);
            String targetServer = parts[2];
            UUID targetUuid = UUID.fromString(parts[3]);
            onCrossTpAccepted(senderUuid, targetServer, targetUuid);
        }
    }

    public void subscribe() {
        JedisService.subscribe(this, CHANNEL);
    }

    protected abstract void onTpaRequested(UUID senderUuid, String senderName, UUID targetUuid);
    protected abstract void onCrossTpAccepted(UUID senderUuid, String targetServer, UUID targetUuid);
}
