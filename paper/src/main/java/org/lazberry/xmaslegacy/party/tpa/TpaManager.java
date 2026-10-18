package org.lazberry.xmaslegacy.party.tpa;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;

import java.util.UUID;

@Registry.Exclude(type = ServerType.LOBBY)
public class TpaManager {
    private static final long TPA_EXPIRATION_SECONDS = 60L;

    public void sendTpaRequest(@NotNull UUID senderUuid, @NotNull UUID targetUuid, @NotNull String senderName, @NotNull String targetServer) {
        String key = "tpa:request:" + targetUuid;
        JedisService.setWithExpire(key, senderUuid.toString(), TPA_EXPIRATION_SECONDS);

        String payload = "REQUEST:" + senderUuid + ":" + senderName + ":" + targetUuid;
        JedisService.publish("xmas:tpa-channel", payload);
    }

    public @Nullable UUID getTpaRequest(@NotNull UUID targetUuid) {
        String senderUuidStr = JedisService.getFromJedis("tpa:request:" + targetUuid);
        if (senderUuidStr == null) return null;
        return UUID.fromString(senderUuidStr);
    }

    public void removeTpaRequest(@NotNull UUID targetUuid) {
        JedisService.removeKey("tpa:request:" + targetUuid);
    }

    public boolean acceptTpaRequest(@NotNull UUID targetUuid, @NotNull Runnable crossServerAction) {
        UUID senderUuid = getTpaRequest(targetUuid);
        if (senderUuid == null) return false;

        removeTpaRequest(targetUuid);

        String senderServer = JedisService.getUserServer(senderUuid);
        String targetServer = JedisService.getUserServer(targetUuid);

        if (senderServer == null || targetServer == null) return false;

        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null && target.isOnline()) {
            cacheTargetLocation(senderUuid, target);
        }

        if (senderServer.equals(targetServer)) {
            return executeLocalTeleport(senderUuid, targetUuid);
        } else {
            crossServerAction.run();
            String payload = "CROSS_TP:" + senderUuid + ":" + targetServer + ":" + targetUuid;
            JedisService.publish("xmas:tpa-channel", payload);
        }
        return true;
    }

    private void cacheTargetLocation(UUID senderUuid, Player target) {
        var loc = target.getLocation();
        String locStr = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
        JedisService.setWithExpire("tpa:target_loc:" + senderUuid, locStr, 30L);
    }

    private boolean executeLocalTeleport(UUID senderUuid, @NotNull UUID targetUuid) {
        Player sender = Bukkit.getPlayer(senderUuid);
        if (sender == null || !sender.isOnline()) return false;

        Player target = Bukkit.getPlayer(targetUuid);
        if (target == null || !target.isOnline()) return false;

        sender.teleportAsync(target.getLocation());
        return true;
    }
}