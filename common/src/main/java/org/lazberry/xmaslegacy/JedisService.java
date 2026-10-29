package org.lazberry.xmaslegacy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.SetParams;
import redis.clients.jedis.resps.Tuple;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Registry.Include(type = ServerType.GLOBAL)
public class JedisService implements Initiator {
	private static final Logger log = LoggerFactory.getLogger(JedisService.class);
	private static RedisClient redisClient = null;
	private static final Set<JedisPubSub> activeSubscribers = ConcurrentHashMap.newKeySet();

	@Override
	public void init() {
		initiateJedis();
	}

	@Override
	public void close() {
		closeJedis();
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

	public static void zadd(String key, String member, double score) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		redisClient.zadd(key, score, member);
	}

	public static List<Tuple> zrevrangeWithScores(String key, long start, long stop) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		return redisClient.zrevrangeWithScores(key, start, stop);
	}

	public static @Nullable Long zrevrank(String key, String member) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		return redisClient.zrevrank(key, member);
	}

	public static @Nullable Double zscore(String key, String member) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");
		return redisClient.zscore(key, member);
	}

	public static void subscribe(JedisPubSub jedisPubSub, String... channels) {
		if (redisClient == null) throw new IllegalStateException("Redis client is not connected! Check host or port.");

		activeSubscribers.add(jedisPubSub);

		Thread.ofVirtual()
				.name("Redis-Subscriber-" + String.join("-", channels))
				.start(() -> {
					try {
						redisClient.subscribe(jedisPubSub, channels);
					} catch (Exception e) {
						if (redisClient == null || !jedisPubSub.isSubscribed()) {
							log.debug("Redis subscriber stopped for channels: {}", String.join(", ", channels));
						} else {
							log.error("Error occurred while subscribing to Redis channels: {}", String.join(", ", channels), e);
						}
					} finally {
						activeSubscribers.remove(jedisPubSub);
					}
				});

		log.info("Started Redis subscriber virtual thread for channels: {}", String.join(", ", channels));
	}

	public static void unsubscribe(JedisPubSub jedisPubSub) {
		if (jedisPubSub == null) return;
		try {
			if (jedisPubSub.isSubscribed()) {
				jedisPubSub.unsubscribe();
			}
		} catch (Exception e) {
			log.warn("Failed to unsubscribe pubsub: {}", e.getMessage());
		} finally {
			activeSubscribers.remove(jedisPubSub);
		}
	}

	public static void initiateJedis() {
		try {
			if (redisClient != null) {
				closeJedis();
			}

			redisClient = RedisClient.builder()
					.hostAndPort("localhost", 6379)
					.build();

			log.info("Redis (RedisClient) initialized successfully.");
		} catch (Exception e) {
			log.error("Failed to initialize Redis: {}", e.getMessage());
		}
	}

	public static void closeJedis() {
		log.info("Closing Redis service and active subscribers (count: {})...", activeSubscribers.size());
		for (JedisPubSub sub : activeSubscribers) {
			try {
				if (sub.isSubscribed()) {
					sub.unsubscribe();
				}
			} catch (Exception e) {
				log.warn("Failed to unsubscribe pubsub during shutdown: {}", e.getMessage());
			}
		}
		activeSubscribers.clear();

		if (redisClient != null) {
			try {
				redisClient.close();
			} catch (Exception e) {
				log.warn("Failed to close Redis client: {}", e.getMessage());
			} finally {
				redisClient = null;
			}
		}
	}
}
