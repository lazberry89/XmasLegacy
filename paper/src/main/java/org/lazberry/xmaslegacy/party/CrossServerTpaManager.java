package org.lazberry.xmaslegacy.party;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
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
    protected void onTpaRequested(UUID senderUuid, String senderName, UUID targetUuid) {
        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null && target.isOnline()) {
            InfoUtils.info(target, "");
        }
    }

    @Override
    protected void onCrossTpAccepted(UUID senderUuid, String targetServer, UUID targetUuid) {
        Player sender = Bukkit.getPlayer(senderUuid);
        if (sender == null || !sender.isOnline()) return;

        log.info("Cross-server TPA triggered for sender {} to move to server {}", sender.getName(), targetServer);

        try {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(targetServer);

            sender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
            sender.sendMessage("§a요청이 수락되었습니다. 대상 서버로 이동합니다...");
        } catch (Exception e) {
            log.error("Failed to send cross-server teleport plugin message for user {}", senderUuid, e);
            sender.sendMessage("§c서버 이동 중 오류가 발생했습니다.");
        }
    }
}