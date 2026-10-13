package org.lazberry.xmaslegacy.party.tpa;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.UUID;

@Registry.Exclude(type = ServerType.LOBBY)
public class TpaManager {
    private static final long TPA_EXPIRATION_SECONDS = 60L;

    public void sendTpaRequest(@NotNull UUID senderUuid, @NotNull UUID targetUuid) {
        String key = "tpa:request:" + targetUuid;
        JedisService.setWithExpire(key, senderUuid.toString(), TPA_EXPIRATION_SECONDS);
    }

    public @Nullable UUID getTpaRequest(@NotNull UUID targetUuid) {
        String senderUuidStr = JedisService.getFromJedis("tpa:request:" + targetUuid);
        if (senderUuidStr == null) return null;
        return UUID.fromString(senderUuidStr);
    }

    public void removeTpaRequest(@NotNull UUID targetUuid) {
        JedisService.removeKey("tpa:request:" + targetUuid);
    }

    public boolean acceptTpaRequest(@NotNull UUID targetUuid) {
        UUID senderUuid = getTpaRequest(targetUuid);
        if (senderUuid == null) return false;

        removeTpaRequest(targetUuid);

        String senderServer = JedisService.getUserServer(senderUuid);
        String targetServer = JedisService.getUserServer(targetUuid);

        if (senderServer == null || targetServer == null) return false;

        if (senderServer.equals(targetServer)) {
            executeLocalTeleport(senderUuid, targetUuid);
        } else {
            String payload = senderUuid + ":" + targetServer + ":" + targetUuid;
            JedisService.publish("xmas:cross_tp", payload);
        }
        return true;
    }
}
