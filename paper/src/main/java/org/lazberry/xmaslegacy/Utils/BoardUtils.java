package org.lazberry.xmaslegacy.Utils;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class BoardUtils {
	/**
	 * Using Cache logic, class saves user's scoreboard Only in runtime for line Updates {@link BoardUtils#setLine(int, Component)}
	 */
	private static final @NotNull Map<UUID, BoardUtils> CACHE = new ConcurrentHashMap<>();

	private final @NotNull @Getter Scoreboard scoreboard;
	private final @NotNull Objective objective;
	private final @NotNull @Getter Player player;
	private final @NotNull Map<Integer, Team> lines = new HashMap<>(20);

	/**
	 * Not only creating board, finds player's {@link BoardUtils} from {@link BoardUtils#CACHE} if exists.
	 * <pre>{@code
	 * BoardUtils.getOrCreate(p, title, b -> {
	 * 	  b.setLine(1, Component.text("a"));
	 * 	  b.setLine(2, Component.text("b"));
	 * });
	 * }</pre>
	 * @param player target player
	 * @param title title of target player's Scoreboard
	 * @param setup Consumer that calls {@link BoardUtils#edit(Consumer)}
	 * @return BoardUtils instance
	 * @see Player
	 * @see Component
	 * @see Consumer
	 */
	@Contract("_, _, _ -> !null")
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
	 * This method must be called when player leaves.
	 * @param player player who leaves or intend to remove Board
	 */
	public static void removeBoard(@NotNull Player player) {
		CACHE.remove(player.getUniqueId());
		player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
	}

	/**
	 * Method that can edit Board using lambda.
	 * <pre>{@code
	 * board.edit(b -> {
	 *     b.updateTitle(Component.text("myTitle"));
	 *     b.setLine(1, Component.text("c"));
	 *     b.setLine(2, Component.text("d"));
	 * });
	 * }</pre>
	 * @param action lambda expressions
	 * @see Component
	 */
	public void edit(@NotNull Consumer<BoardUtils> action) {
		action.accept(this);
	}

	/**
	 * Constructor of Utility, only called by internal of class.
	 * @param player target to make {@link Scoreboard} for.
	 * @param title Component of title to show as {@link Scoreboard} title.
	 * @see	BoardUtils#updateTitle(Component)
	 */
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

	/**
	 * Change player's Scoreboard title without blinking sideEffect.
	 * @param title {@link Component} title to change.
	 */
	public void updateTitle(@NotNull Component title) {
		this.objective.displayName(title);
	}

	/**
	 * Update or Set line of Scoreboard without blinking sideEffect by only changing inner Component in line.
	 * @param line which line to edit or add
	 * @param text {@link Component} text to use as target line's component
	 */
	public void setLine(int line, @NotNull Component text) {
		if (line < 0 || line > 14) return;

		Team team = lines.get(line);
		if (team != null) {
			team.prefix(text);

			String invisibleEntry = getInvisibleEntry(line);
			this.objective.getScore(invisibleEntry).setScore(15 - line);
		}
	}

	/**
	 * Removing line of Scoreboard.
	 * @param line target line to remove
	 */
	public void removeLine(int line) {
		if (line < 0 || line > 14) return;
		String invisibleEntry = getInvisibleEntry(line);
		this.scoreboard.resetScores(invisibleEntry);
	}

	private @NotNull String getInvisibleEntry(int line) {
		return "§" + Integer.toHexString(line) + "§r";
	}
}