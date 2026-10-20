package org.lazberry.xmaslegacy.rank;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NonBlocking;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.rank.database.SqlRankRepository;
import org.lazberry.xmaslegacy.user.User;
import redis.clients.jedis.resps.Tuple;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class RankManager implements Initiator {
	private static final String dollarChannel = "rank:dollar";
	private static final String expChannel = "rank:exp";
	private static final String rxpChannel = "rank:rxp";
	private static final String playtimeChannel = "rank:playtime";

	private final SqlRankRepository rankRepository;

	@Inject
	public RankManager(SqlRankRepository rankRepository) {
		this.rankRepository = rankRepository;
	}

	@Override
	public void init() {
		initiate();
	}

	private void initiate() {
		Arrays.stream(RankType.values()).forEach(rankType ->
				rankRepository.loadRankData(rankType).whenComplete((rank, throwable) -> {
					if (throwable != null) {
						log.error("Failed to load rank data for rank type {}", rank, throwable);
						return;
					}
					warmUp(rankType, rank);
				}));
	}

	private String getChannelByRankType(RankType type) {
		return switch (type) {
			case DOLLAR -> dollarChannel;
			case PLAYTIME -> playtimeChannel;
			case EXP -> expChannel;
			case ROLE_EXP -> rxpChannel;
		};
	}

	public void updateUser(User user) {
		var uuid = user.getUniqueId();

		update(RankType.DOLLAR, uuid, user.getDollars());
		update(RankType.EXP, uuid, user.getExp());
		update(RankType.ROLE_EXP, uuid, user.getRoleExp());
		update(RankType.PLAYTIME, uuid, user.getPlayTime());
	}

	public void warmUp(RankType rankType, Map<UUID, Double> values) {
		String channel = getChannelByRankType(rankType);
		for (var entry : values.entrySet()) {
			String uuid = entry.getKey().toString();
			double value = entry.getValue();

			JedisService.zadd(channel, uuid, value);
		}
	}

	public void update(RankType rankType, UUID uuid, double value) {
		JedisService.zadd(getChannelByRankType(rankType), uuid.toString(), value);
	}

	@NonBlocking
	public CompletableFuture<ArrayList<RankData>> getRankByLimit(RankType rankType, int limit) {
		return CompletableFuture.supplyAsync(() -> {
			synchronized (this) {
				String channel = getChannelByRankType(rankType);

				List<Tuple> tuples = JedisService.zrevrangeWithScores(channel, 0, limit - 1);
				ArrayList<RankData> rankList = new ArrayList<>();

				int rank = 1;
				for (Tuple tuple : tuples) {
					UUID uuid = UUID.fromString(tuple.getElement());
					double score = tuple.getScore();

					rankList.add(new RankData(rank++, uuid, score));
				}

				return rankList;
			}
		});
	}

	public record RankData(int rank, UUID uuid, double value) {}
}
