package org.lazberry.xmaslegacy.Utils;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.ApiStatus;

public final class GlowUtils {

	@ApiStatus.Internal
	private GlowUtils() {
		throw new UnsupportedOperationException("Utility class");
	}

	public static void setGlowColor(Entity entity, NamedTextColor color) {
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
    public static void clearGlow(Entity entity) {
        entity.setGlowing(false);

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getEntryTeam(entity.getUniqueId().toString());

        if (team != null) {
            team.removeEntry(entity.getUniqueId().toString());
        }
    }
}
