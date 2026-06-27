package xmaslegacy.Ranks;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.RankType;
import org.lazberry.xmaslegacy.User.User;
import xmaslegacy.Utils.KeyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum RankBoardSystem {
	INSTANCE;

	private final @NotNull RankingSystem ranks;
	private final @NotNull Map<String, TextDisplay> board = new HashMap<>();

	RankBoardSystem() {
		this.ranks = RankingSystem.INSTANCE;
	}

	public void update(@NotNull String name) {

	}

	public void spawn(@NotNull RankType type, @NotNull String name, @NotNull Location loc) {
		loc.getWorld().spawn(loc, TextDisplay.class, t -> {
			this.board.put(name, t);
			t.setBillboard(Display.Billboard.FIXED);
			t.text(this.rankComponent(type, 10));
			t.setAlignment(TextDisplay.TextAlignment.CENTER);
			t.setBrightness(new Display.Brightness(15, 15));
			t.setRotation(loc.getYaw(), loc.getPitch());
		});
	}

	public void resetBoards() {
		this.board.values().stream()
				.filter(Entity::isValid)
				.forEach(Entity::remove);
		this.board.clear();
	}
}
