package org.lazberry.xmaslegacy.api.donate;

import lombok.extern.slf4j.Slf4j;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.api.ProductHandler;
import org.lazberry.xmaslegacy.api.ProductRecordManager;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import redis.clients.jedis.JedisPubSub;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class DonationGrantSubscriber extends JedisPubSub implements Initiator {
	private final ProductActionManager actionManager;
	private final ProductRecordManager recordManager;
	private final String channel;

	@Inject
	public DonationGrantSubscriber(ProductActionManager actionManager, ProductRecordManager recordManager) {
		this.actionManager = actionManager;
		this.recordManager = recordManager;
		this.channel = ProductHandler.channel;
	}

	@Override
	public void init() {
		JedisService.subscribe(this, channel);
	}

	@Override
	public void close() {
		JedisService.unsubscribe(this);
	}

	@Override
	public void onMessage(String channel, String message) {
		if (!channel.equals(this.channel)) return;

		// The message is the purchase number; lookup, claim and item grant all happen in grantProduct.
		try {
			actionManager.grantProduct(Long.parseLong(message));
		} catch (NumberFormatException e) {
			log.warn("Invalid purchase id received: {}", message);
		}
	}
}
