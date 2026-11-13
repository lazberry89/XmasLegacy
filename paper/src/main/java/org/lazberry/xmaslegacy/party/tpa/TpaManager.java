package org.lazberry.xmaslegacy.party.tpa;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;

import java.util.UUID;

@Components.Exclude(type = ServerType.LOBBY)
public class TpaManager {
    private static final long TPA_EXPIRATION_SECONDS = 60L;

    /**
     * Stores the request for 60 seconds and notifies the target, wherever it is on the network.
     * A target keeps one request at a time: a newer one replaces the older.
     * @return false if the target is the sender itself or is not online on the network.
     */
    public boolean sendTpaRequest(@NotNull UUID senderUuid, @NotNull String senderName, @NotNull UUID targetUuid) {
        if (senderUuid.equals(targetUuid) || JedisService.getUserServer(targetUuid) == null) return false;

        JedisService.setWithExpire("tpa:request:" + targetUuid, senderUuid.toString(), TPA_EXPIRATION_SECONDS);
        JedisService.publish("xmas:tpa-channel", "REQUEST:" + senderUuid + ":" + senderName + ":" + targetUuid);
        return true;
    }

    public @Nullable UUID getTpaRequest(@NotNull UUID targetUuid) {
        String senderUuidStr = JedisService.getFromJedis("tpa:request:" + targetUuid);
        if (senderUuidStr == null) return null;
        return UUID.fromString(senderUuidStr);
    }

    public void removeTpaRequest(@NotNull UUID targetUuid) {
        JedisService.removeKey("tpa:request:" + targetUuid);
    }

    /**
     * Accepts the request waiting for this player and brings the requester here.
     * Must be called on the target's own server (the target runs /tpaccept there).
     * @return false if there is no request, or the requester is no longer online.
     */
    public boolean acceptTpaRequest(@NotNull Player target) {
        UUID targetUuid = target.getUniqueId();
        UUID senderUuid = getTpaRequest(targetUuid);
        if (senderUuid == null) return false;

        // The request is spent either way: a requester who went offline can't be brought here later.
        removeTpaRequest(targetUuid);

        String senderServer = JedisService.getUserServer(senderUuid);
        String targetServer = JedisService.getUserServer(targetUuid);
        if (senderServer == null || targetServer == null) return false;

        if (senderServer.equals(targetServer)) {
            Player sender = Bukkit.getPlayer(senderUuid);
            if (sender == null) return false;
            sender.teleportAsync(target.getLocation());
            return true;
        }

        // Cross-server only: TpaListener reads this when the requester arrives. Caching it for a
        // same-server teleport would drag the requester back here on their next join.
        var loc = target.getLocation();
        String locStr = loc.getWorld().getName() + "," + loc.getX() + "," + loc.getY() + "," + loc.getZ() + "," + loc.getYaw() + "," + loc.getPitch();
        JedisService.setWithExpire("tpa:target_loc:" + senderUuid, locStr, 30L);

        JedisService.publish("xmas:tpa-channel", "CROSS_TP:" + senderUuid + ":" + targetServer + ":" + targetUuid);
        return true;
    }
}
