package org.lazberry.xmaslegacy.party;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import org.lazberry.xmaslegacy.Velocity;

import java.util.UUID;

public class TpaActualTeleportManager extends AbstractVelocityTpaSubscriber {
	private final ProxyServer proxy;

	public TpaActualTeleportManager() {
		this.proxy = Velocity.getInstance();
		subscribe();
	}

	@Override
	protected void onCrossTpAccepted(UUID senderUuid, String targetServer) {
		Player player = proxy.getPlayer(senderUuid).orElse(null);
		RegisteredServer server = proxy.getServer(targetServer).orElse(null);

		if (player != null && server != null) {
			player.createConnectionRequest(server).connect().thenAccept(result -> {
				if (!result.isSuccessful()) {
					player.sendMessage(Component.text("§c서버 이동에 실패했습니다: " + result));
				}
			});
		}
	}
}
