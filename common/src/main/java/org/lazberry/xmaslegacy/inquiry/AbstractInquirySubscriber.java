package org.lazberry.xmaslegacy.inquiry;

import org.lazberry.xmaslegacy.JedisService;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public abstract class AbstractInquirySubscriber extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        if (!"inquiry-channel".equals(channel)) return;

        String[] parts = message.split(":", 3);
        if (parts.length < 3) return;

        String type = parts[0];

        if ("NEW_INQUIRY".equals(type)) {
            String playerName = parts[1];
            String idStr = parts[2];
            onInquiryReceived(playerName, idStr);
        }
        else if ("ANSWERED".equals(type)) {
            // 💡 답변 완료 시 유저 알림 트리거
            java.util.UUID userUuid = java.util.UUID.fromString(parts[1]);
            String idStr = parts[2];
            onInquiryAnswered(userUuid, idStr);
        }
    }

    public void subscribeJedisService(AbstractInquirySubscriber instance) {
        JedisService.subscribe(instance, "inquiry-channel");
    }

    protected abstract void onInquiryReceived(String playerName, String ticketId);
    protected abstract void onInquiryAnswered(UUID userUuid, String ticketId);
}
