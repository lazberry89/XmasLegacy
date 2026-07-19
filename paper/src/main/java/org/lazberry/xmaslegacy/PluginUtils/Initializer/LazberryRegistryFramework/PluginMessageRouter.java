package org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public final class PluginMessageRouter implements PluginMessageListener {
    private final Map<String, List<PluginReceiver>> routes = new HashMap<>();
    private static final String icon = LazberryRegistryFramework.icon(false);

    public void registerRoute(@NotNull String channel, @NotNull PluginReceiver receiver) {
        routes.computeIfAbsent(channel, k -> new ArrayList<>()).add(receiver);
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        List<PluginReceiver> receivers = routes.get(channel);
        if (receivers == null || receivers.isEmpty()) return;

        String content = new String(message, StandardCharsets.UTF_8);

        if (LazberryRegistryFramework.isDebug()) {
            log.info("{} [Network Inbound] Channel: {} | Content Length: {}", icon, channel, content.length());
        }

        for (PluginReceiver receiver : receivers) {
            try {
                receiver.receive(content);
            } catch (Exception e) {
                log.error("{} Exception occurred in Receiver for channel: {}", icon, channel, e);
            }
        }
    }
}
