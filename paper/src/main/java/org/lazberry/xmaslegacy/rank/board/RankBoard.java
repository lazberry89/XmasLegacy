package org.lazberry.xmaslegacy.rank.board;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lazberry.xmaslegacy.rank.RankManager;
import org.lazberry.xmaslegacy.rank.RankType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/** One floating board. The world is kept by name because it may be loaded after this plugin. */
public class RankBoard {
	private static final long TREND_PERIOD = 10 * 60 * 1000L;
	private static final float SCALE = 1.15f;

	@Getter private final RankType rankType;
	@Getter private final String world;
	@Getter private final double x, y, z;
	@Getter private final int printLimit;
	private @Nullable TextDisplay display;
	private @Nullable Component shown;

	/** Ranks at the last snapshot; arrows compare with this, so they stay visible instead of one refresh. */
	private Map<UUID, Integer> baseline = Map.of();
	private long baselineTime;

	public RankBoard(@NotNull RankType rankType, @NotNull String world, double x, double y, double z, int printLimit) {
		this.rankType = rankType;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.printLimit = printLimit;
	}

	public void update(@NotNull List<RankManager.RankData> ranks, @NotNull Function<UUID, String> names) {
		World w = Bukkit.getWorld(world);
		if (w == null || !w.isChunkLoaded((int) Math.floor(x) >> 4, (int) Math.floor(z) >> 4)) return; // nobody can see it

		Component text = RankBoardText.render(rankType, ranks.subList(0, Math.min(printLimit, ranks.size())), baseline, names);
		refreshBaseline(ranks);

		if (display == null || !display.isValid()) {
			display = spawn(new Location(w, x, y, z));
			shown = null;
		}
		if (!text.equals(shown)) {
			display.text(text);
			shown = text;
		}
	}

	private void refreshBaseline(List<RankManager.RankData> ranks) {
		long now = System.currentTimeMillis();
		if (ranks.isEmpty() || now - baselineTime < TREND_PERIOD) return;
		Map<UUID, Integer> current = new HashMap<>();
		ranks.forEach(r -> current.put(r.uuid(), r.rank()));
		baseline = current;
		baselineTime = now;
	}

	private static TextDisplay spawn(Location location) {
		return location.getWorld().spawn(location, TextDisplay.class, t -> {
			// Never saved with the chunk: a crash would otherwise leave a frozen copy behind.
			t.setPersistent(false);
			t.setBillboard(Display.Billboard.VERTICAL);
			t.setAlignment(TextDisplay.TextAlignment.CENTER);
			t.setBackgroundColor(Color.fromARGB(190, 10, 18, 34));
			t.setShadowed(true);
			t.setLineWidth(400);
			t.setBrightness(new Display.Brightness(15, 15));
			Transformation base = t.getTransformation();
			t.setTransformation(new Transformation(base.getTranslation(), base.getLeftRotation(),
					new Vector3f(SCALE, SCALE, SCALE), base.getRightRotation()));
		});
	}

	public void removeDisplay() {
		if (display != null) display.remove();
		display = null;
		shown = null;
	}
}
