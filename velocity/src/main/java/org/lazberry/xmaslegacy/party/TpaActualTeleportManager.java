package org.lazberry.xmaslegacy.party;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.lazberry.xmaslegacy.Velocity;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.util.UUID;

@Components
public class TpaActualTeleportManager extends AbstractVelocityTpaSubscriber implements Initiator {
	private final ProxyServer proxy;

	@Inject
	public TpaActualTeleportManager() {
		this.proxy = Velocity.getInstance();
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
	protected void onCrossTpAccepted(UUID senderUuid, String targetServer) {
		Player player = proxy.getPlayer(senderUuid).orElse(null);
		RegisteredServer server = proxy.getServer(targetServer).orElse(null);

		if (player == null) return;
		if (server == null) {
			player.sendMessage(Component.text("§c이동할 서버를 찾을 수 없습니다: " + targetServer));
			return;
		}

		player.createConnectionRequest(server).connect().thenAccept(result -> {
			if (!result.isSuccessful()) {
				player.sendMessage(Component.text("§c서버 이동에 실패했습니다: " + result));
			}
		});
	}

}
