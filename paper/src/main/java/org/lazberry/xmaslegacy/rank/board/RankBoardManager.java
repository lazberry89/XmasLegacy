package org.lazberry.xmaslegacy.rank.board;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.rank.ProxyRankManager;
import org.lazberry.xmaslegacy.rank.RankType;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Registry.Exclude(type = ServerType.LOBBY)
public class RankBoardManager implements Initiator {
	private final Map<RankType, RankBoard> rankBoards = new ConcurrentHashMap<>(8);
    private final ProxyRankManager rankManager;
	private final RankBoardConfig boardConfig;

    @Inject
    public RankBoardManager(ProxyRankManager rankManager, RankBoardConfig boardConfig) {
        this.rankManager = rankManager;
	    this.boardConfig = boardConfig;
    }

    @Override
    public void init() {
		rankBoards.clear();
		rankBoards.putAll(boardConfig.loadSync());
		spawnAll();
    }

	private void spawnAll() {
		rankBoards.values().forEach(rankBoard ->
			rankBoard.spawnOrUpdateDisplay(rankManager.getRankList(rankBoard.getRankType())));
	}

	private void removeAll() {
		rankBoards.values().forEach(RankBoard::removeDisplay);
	}

	public void createRankBoard(RankType type, @NotNull Location loc, int limit) {
		removeRankBoard(type);

		loc.getChunk().load(true);

		var board = new RankBoard(type, loc, limit);
		board.spawnOrUpdateDisplay(rankManager.getRankList(type));
		rankBoards.put(type, board);
	}

	public boolean removeRankBoard(RankType type) {
		var board = rankBoards.remove(type);
		if (board == null) return false;
		board.removeDisplay();
		return true;
	}

	public void updateBoard(RankType type) {
		var board = rankBoards.get(type);
		if (board != null) board.spawnOrUpdateDisplay(rankManager.getRankList(type));
	}

	public void updateAll() {
		Arrays.stream(RankType.values()).forEach(this::updateBoard);
	}

    @Override
    public void close() {
        boardConfig.saveSync(rankBoards);
		removeAll();
		rankBoards.clear();
    }
}
