package org.lazberry.xmaslegacy.Ranks;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.RankType;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.Utils.KeyUtils;

import java.util.List;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RankBoard {
	@EqualsAndHashCode.Include
	private final @NotNull @Getter NamespacedKey key;
	private final @NotNull @Getter String name;
	private final @NotNull @Getter RankType type;
	private final @Getter int amount;
	private @Getter @Setter TextDisplay display;

	public RankBoard(@NotNull String name, @NotNull RankType type, int amount) {
		this.key = KeyUtils.get("rank_board");
		this.name = name;
		this.type = type;
		this.amount = amount;
	}

	private @NotNull Component rankComponent(@NotNull RankType type, int amount) {
		List<User> users = RankingSystem.INSTANCE.rank(type);
		StringBuilder sb = new StringBuilder();

		Component title = ColorUtils.chat("🏆 " + type.name().toUpperCase() + " &#FF4545R&#FB5E39A&#F7762DN&#F28F21K&#EEA715 🏆");

		for (int i = 0; i < amount; i++) {
			String prefix = switch (i) {
				case 0 -> "&c&l1위 ";
				case 1 -> "&6&l2위 ";
				case 2 -> "&e&l3위 ";
				default -> "&7&l" + (i + 1) + "위 ";
			};

			String name = (i < users.size()) ? users.get(i).getName() : "&7&l-";
			sb.append(prefix).append(name).append("\n");
		}

		return title.appendNewline().append(ColorUtils.chat(sb.toString()));
	}

	public void spawn(@NotNull Location loc) {
		loc.getWorld().spawn(loc, TextDisplay.class, t -> {
			t.setBillboard(Display.Billboard.FIXED);
			t.text(this.rankComponent(type, amount));
			t.setAlignment(TextDisplay.TextAlignment.CENTER);
			t.setBrightness(new Display.Brightness(15, 15));
			t.setRotation(loc.getYaw(), 0);
			t.getPersistentDataContainer().set(key, PersistentDataType.STRING, name);
			this.display = t;
		});
	}

	public void update() {
		if (this.display == null || !this.display.isValid()) return;
		this.display.text(this.rankComponent(type, amount));
	}
}
