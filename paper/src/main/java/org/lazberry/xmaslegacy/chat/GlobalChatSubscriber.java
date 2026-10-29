package org.lazberry.xmaslegacy.chat;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import redis.clients.jedis.JedisPubSub;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class GlobalChatSubscriber extends JedisPubSub implements Initiator {
    private static final String CHANNEL = "global_chat";

    @Override
    public void init() {
        JedisService.subscribe(this, CHANNEL);
    }

    @Override
    public void onMessage(String channel, String message) {
        if (!CHANNEL.equals(channel)) return; //least protection of channel difference

        ChatPacket receivedPacket = ChatPacket.fromJson(message);
        if (receivedPacket == null) {
            log.error("Failed to parse global chat packet. Ignoring message.");
            return;
        }
        Component finalMessage = receivedPacket.message();
        Bukkit.broadcast(finalMessage);
    }
}
