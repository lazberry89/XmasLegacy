package org.lazberry.xmaslegacy.scoreboard;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.BoardUtils;

import java.util.function.Consumer;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class ScoreboardReloadHandler implements Initiator {
	private final UserScoreboard scoreboard;

	@Inject
	public ScoreboardReloadHandler(UserScoreboard scoreboard) {
		this.scoreboard = scoreboard;
	}

	@Override
	public void init() {
		forEachPlayers(scoreboard::updateUserBoard);
		log.info("Loaded online player's scoreboard");
	}

	private void forEachPlayers(Consumer<Player> consumer) {
		Bukkit.getOnlinePlayers().stream()
				.filter(Player::isValid)
				.forEach(consumer);
	}

	@Override
	public void close() {
		forEachPlayers(BoardUtils::removeBoard);
	}
}
