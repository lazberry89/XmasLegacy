package org.lazberry.xmaslegacy.catalog;

import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import redis.clients.jedis.JedisPubSub;

/** Reloads the samples when an admin saved them on any server. */
@Components.Exclude(type = ServerType.LOBBY)
public class CatalogSampleSubscriber extends JedisPubSub implements Initiator {
	private final CatalogManager manager;

	@Inject
	public CatalogSampleSubscriber(CatalogManager manager) {
		this.manager = manager;
	}

	@Override
	public void init() {
		JedisService.subscribe(this, CatalogManager.SAMPLE_CHANNEL);
	}

	@Override
	public void close() {
		JedisService.unsubscribe(this);
	}

	@Override
	public void onMessage(String channel, String message) {
		if (channel.equals(CatalogManager.SAMPLE_CHANNEL)) manager.loadSamples();
	}
}
