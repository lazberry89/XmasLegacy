package org.lazberry.xmaslegacy.rank.board;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.rank.ProxyRankManager;
import org.lazberry.xmaslegacy.rank.RankType;

import java.util.EnumMap;
import java.util.Map;

/** Main thread only. */
@Components.Exclude(type = ServerType.LOBBY)
public class RankBoardManager implements Initiator {
	private final Map<RankType, RankBoard> rankBoards = new EnumMap<>(RankType.class);
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
		rankBoards.putAll(boardConfig.load());
		updateAll();
	}

	/** @return false if the board is up but could not be saved to the file. */
	public boolean createRankBoard(@NotNull RankType type, @NotNull Location loc, int limit) {
		var old = rankBoards.remove(type);
		if (old != null) old.removeDisplay();

		var board = new RankBoard(type, loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), limit);
		rankBoards.put(type, board);
		update(board);
		return boardConfig.save(rankBoards);
	}

	public boolean removeRankBoard(@NotNull RankType type) {
		var board = rankBoards.remove(type);
		if (board == null) return false;
		board.removeDisplay();
		boardConfig.save(rankBoards);
		return true;
	}

	private void update(RankBoard board) {
		board.update(rankManager.getRankList(board.getRankType()), rankManager::getName);
	}

	public void updateAll() {
		rankBoards.values().forEach(this::update);
	}

	/** Nothing is saved here: create and remove already did, and a failed load must not overwrite the file. */
	@Override
	public void close() {
		rankBoards.values().forEach(RankBoard::removeDisplay);
		rankBoards.clear();
	}
}
