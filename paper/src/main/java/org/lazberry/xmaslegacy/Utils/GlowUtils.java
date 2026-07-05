package org.lazberry.xmaslegacy.Utils;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public final class GlowUtils {

	@ApiStatus.Internal
	private GlowUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * This method glows target entity with param2 color.
	 * Also apply team to entity to change color.
	 * <pre>{@code
	 * GlowUtils.glow(target, NamedTextColor.YELLOW);
	 * }</pre>
	 * @param entity Target entity to give glow.
	 * @param color target color to glow.
	 */
	public static void glow(@NotNull Entity entity, @NotNull NamedTextColor color) {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		String teamName = "glow_" + color.toString();
		Team team = scoreboard.getTeam(teamName);

		if (team == null) {
			team = scoreboard.registerNewTeam(teamName);
			team.color(color);
		}

		team.addEntity(entity);
		entity.setGlowing(true);
	}

	/**
	 * Remove target entity's glowing.Also remove team entry for memory cleaning.
	 * <pre>{@code
	 * GlowUtils.clearGlow(target);
	 * }</pre>
	 * @param entity target entity.
	 */
    public static void clearGlow(@NotNull Entity entity) {
        entity.setGlowing(false);

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getEntryTeam(entity.getUniqueId().toString());

        if (team != null) {
            team.removeEntry(entity.getUniqueId().toString());
        }
    }
}
