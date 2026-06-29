package xmaslegacy.Ranks;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.User.RankType;
import xmaslegacy.Annotation.Task;
import xmaslegacy.PluginUtils.ServerType;
import xmaslegacy.PluginUtils.Tasks;
import xmaslegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Task(type = ServerType.GLOBAL)
public enum RankBoardSystem implements Tasks {
	INSTANCE;

	private final @NotNull Map<String, RankBoard> board = new HashMap<>();
	private @Nullable BukkitTask task;

	RankBoardSystem() {}

	public @NotNull RankBoard spawn(@NotNull String name, @NotNull RankType type, int amount, @NotNull Location loc) {
		if (this.board.containsKey(name)) return this.board.get(name);

		RankBoard rankBoard = new RankBoard(name, type, amount);
		rankBoard.spawn(loc);
		this.board.put(name, rankBoard);

		log.info("Rank board {} spawned.", name);
		return rankBoard;
	}

	public boolean remove(@NotNull String name) {
		RankBoard rb = this.board.get(name);
		if (rb == null) return false;

		TextDisplay td = rb.getDisplay();
		if (td != null) td.remove();
		this.board.remove(name);
		return true;
	}

	public void resetBoards() {
		this.board.values().stream()
				.map(RankBoard::getDisplay)
				.filter(Objects::nonNull)
				.forEach(Entity::remove);
		this.board.clear();
	}

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		this.resetBoards();
		if (this.task != null) return;
		log.warn("Board task started. {} board enabled.", board.size());
		this.task = Bukkit.getScheduler().runTaskTimer(plugin, () ->
			this.board.values().stream()
					.filter(r -> r.getDisplay() != null)
					.filter(r -> r.getDisplay().isValid())
					.forEach(RankBoard::update), 0L, 30L);
	}

	@Override
	public void stopTask() {
		if (this.task == null) return;
		this.task.cancel();
		this.task = null;

		this.resetBoards();
	}
}
