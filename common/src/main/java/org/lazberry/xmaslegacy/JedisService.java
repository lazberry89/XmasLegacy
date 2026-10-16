package org.lazberry.xmaslegacy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.SetParams;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Registry.Include(type = ServerType.GLOBAL)
public class JedisService implements Initiator {
	private static final Logger log = LoggerFactory.getLogger(JedisService.class);
	private static RedisClient redisClient = null;

	@Override
	public void init() {
		initiateJedis();
	}

	public static void setUserServer(@NotNull UUID uuid, @NotNull String serverName) {
		setToJedis("user:" + uuid + ":server", serverName);
	}

	public static @Nullable String getUserServer(@NotNull UUID uuid) {
		return getFromJedis("user:" + uuid + ":server");
	}

	public static void removeUserServer(@NotNull UUID uuid) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		redisClient.del("user:" + uuid + ":server");
	}

	public static void setToJedis(String key, String value) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		redisClient.set(key, value);
	}

	public static String getFromJedis(String key) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		return redisClient.get(key);
	}

	public static void removeKey(String key) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		redisClient.del(key);
	}

	public static void setWithExpire(String key, String value, long seconds) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		redisClient.set(key, value, SetParams.setParams().ex(seconds));
	}

	public static void publish(String channel, String message) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		redisClient.publish(channel, message);
	}

	public static long ttl(String key) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected!");
		return redisClient.ttl(key);
	}

	public static Set<String> smembers(String key) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		return redisClient.smembers(key);
	}

	public static List<String> lrange(String key, long from, long to) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		return redisClient.lrange(key, from, to);
	}

	public static List<String> lrange(String key) {
		return lrange(key, 0, -1);
	}

	public static void sadd(String key, String... members) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		redisClient.sadd(key, members);
	}

	public static void srem(String key, String... members) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		redisClient.srem(key, members);
	}

	public static void subscribe(JedisPubSub jedisPubSub, String... channels) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");

		Thread.ofVirtual()
				.name("Redis-Subscriber-Thread")
				.start(() -> {
					try {
						redisClient.subscribe(jedisPubSub, channels);
					} catch (Exception e) {
						log.error("Error occurred while subscribing to Redis channels: {}", String.join(", ", channels), e);
					}
				});

		log.info("Started Redis subscriber virtual thread for channels: {}", String.join(", ", channels));
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

	public static void closeJedis() {
		if (redisClient != null) redisClient.close();
	}

	@Override
	public void close() {
		closeJedis();
	}
}
