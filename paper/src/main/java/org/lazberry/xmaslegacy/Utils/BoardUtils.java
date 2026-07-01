package org.lazberry.xmaslegacy.Utils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class BoardUtils {
	private static final @NotNull Map<UUID, BoardUtils> CACHE = new ConcurrentHashMap<>();

	private final @NotNull @Getter Scoreboard scoreboard;
	private final @NotNull Objective objective;
	private final @NotNull @Getter Player player;
	private final @NotNull Map<Integer, Team> lines = new HashMap<>(20);

	/**
	 * 💡 기존 create를 대체하는 스마트한 메서드입니다.
	 * 이미 보드가 있으면 기존 보드를 수정(업데이트)하고, 없으면 새로 만듭니다.
	 */
	@CanIgnoreReturnValue
	public static @NotNull BoardUtils getOrCreate(@NotNull Player player, @NotNull Component title, @NotNull Consumer<BoardUtils> setup) {
		UUID uuid = player.getUniqueId();

		if (CACHE.containsKey(uuid)) {
			BoardUtils board = CACHE.get(uuid);
			board.updateTitle(title);
			board.edit(setup);
			return board;
		}

		BoardUtils board = new BoardUtils(player, title);
		setup.accept(board);
		CACHE.put(uuid, board);
		return board;
	}

	/**
	 * 💡 [중요] 플레이어가 서버를 나갈 때 반드시 호출해 주어야 합니다. (메모리 누수 방지)
	 */
	public static void removeBoard(@NotNull Player player) {
		CACHE.remove(player.getUniqueId());
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	public void edit(@NotNull Consumer<BoardUtils> action) {
		action.accept(this);
	}

	@ApiStatus.Internal
	private BoardUtils(@NotNull Player player, @NotNull Component title) {
		this.player = player;
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.objective = scoreboard.registerNewObjective("party_board", Criteria.DUMMY, title);
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		for (int i = 0; i < 15; i++) {
			Team team = scoreboard.registerNewTeam("line_" + i);
			String invisibleEntry = getInvisibleEntry(i);
			team.addEntry(invisibleEntry);
			this.lines.put(i, team);
		}

		player.setScoreboard(this.scoreboard);
	}

	public void updateTitle(@NotNull Component title) {
		this.objective.displayName(title);
	}

	public void setLine(int line, @NotNull Component text) {
		if (line < 0 || line > 14) return;

		Team team = lines.get(line);
		if (team != null) {
			team.prefix(text);

			String invisibleEntry = getInvisibleEntry(line);
			this.objective.getScore(invisibleEntry).setScore(15 - line);
		}
	}

	public void removeLine(int line) {
		if (line < 0 || line > 14) return;
		String invisibleEntry = getInvisibleEntry(line);
		this.scoreboard.resetScores(invisibleEntry);
	}

	private @NotNull String getInvisibleEntry(int line) {
		return "§" + Integer.toHexString(line) + "§r";
	}
}