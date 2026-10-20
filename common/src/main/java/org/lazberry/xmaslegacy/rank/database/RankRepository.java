package org.lazberry.xmaslegacy.rank.database;

import org.lazberry.xmaslegacy.rank.RankType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface RankRepository {
	CompletableFuture<Map<UUID, Double>> loadRankData(RankType rankType);
}
