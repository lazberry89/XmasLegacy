package org.lazberry.xmaslegacy;

import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.RedisClient;

@Registry.Exclude(type = ServerType.LOBBY)
public class JedisService implements Initiator {
	private static final Logger log = LoggerFactory.getLogger(JedisService.class);
	private static RedisClient redisClient = null;

	@Override
	public void init() {
		initiateJedis();
	}

	public static void setToJedis(String key, String value) {
		if (redisClient == null) return;
		redisClient.set(key, value);
	}

	public static String getFromJedis(String key) {
		if (redisClient == null) return null;
		return redisClient.get(key);
	}

	public static void initiateJedis() {
		try {
			redisClient = RedisClient.builder()
					.hostAndPort("localhost", 6379)
					.build();

			log.info("Redis (RedisClient) initialized successfully.");
		} catch (Exception e) {
			log.error("Failed to initialize Redis: {}", e.getMessage());
		}
	}

	private static void closeJedis() {
		if (redisClient != null) redisClient.close();
	}

	@Override
	public void close() {
		closeJedis();
	}
}
