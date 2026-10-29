package org.lazberry.xmaslegacy.party;

import com.velocitypowered.api.proxy.ProxyServer;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.Velocity;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

public abstract class AbstractVelocityTpaSubscriber extends JedisPubSub {
	private static final String CHANNEL = "xmas:tpa-channel";
	private final ProxyServer proxy;

	protected AbstractVelocityTpaSubscriber() {
		this.proxy = Velocity.getInstance();
	}

	@Override
	public void onMessage(String channel, String message) {
		if (!CHANNEL.equals(channel)) return;

		String[] parts = message.split(":", 4);
		if (parts.length < 4) return;

		String type = parts[0];

		if ("CROSS_TP".equals(type)) {
			UUID senderUuid = UUID.fromString(parts[1]);
			String targetServer = parts[2];

			proxy.getScheduler().buildTask(Velocity.getVelocityInstance(), () ->
				onCrossTpAccepted(senderUuid, targetServer)).schedule();
		}
	}

	public void subscribe() {
		JedisService.subscribe(this, CHANNEL);
	}

	public void unsubscribeSubscriber() {
		JedisService.unsubscribe(this);
	}

	protected abstract void onCrossTpAccepted(UUID senderUuid, String targetServer);
}
