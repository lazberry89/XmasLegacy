package xmaslegacy.Ranks;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.RankType;
import org.lazberry.xmaslegacy.User.User;

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

	private @NotNull Component rankComponent(@NotNull List<User> users, int amount) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < amount; i++) {
			String prefix = switch (i) {
				case 0 -> "&c&l1위 ";
				case 1 -> "&6&l2위 ";
				case 2 -> "&e&l3위 ";
				default -> "&7&l" + (i + 1) + "위 ";
			};

			String name = (i < users.size()) ? users.get(i).getName() : "-";
			sb.append(prefix).append(name).append("\n");
		}

		return ColorUtils.chat(sb.toString());
	}

	public void spawn(@NotNull RankType type, @NotNull String name, @NotNull Location loc) {
		loc.getWorld().spawn(loc, TextDisplay.class, t -> {
			this.board.put(name, t);
			t.setBillboard(Display.Billboard.FIXED);
			t.text(this.rankComponent(ranks.rank(type), 10));
			t.setAlignment(TextDisplay.TextAlignment.CENTER);
			t.setBrightness(new Display.Brightness(15, 15));
			t.setRotation(loc.getYaw(), loc.getPitch());
		});
	}
}
