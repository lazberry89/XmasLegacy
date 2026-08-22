package org.lazberry.xmaslegacy.utils;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Shulker;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;

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

		String teamName = "glow_" + color;
		Team team = scoreboard.getTeam(teamName);

		if (team == null) {
			team = scoreboard.registerNewTeam(teamName);
			team.color(color);
		}

		team.addEntry(entity.getUniqueId().toString());
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

	/**
	 * This method glows block. Spawns invisible shulker and give it a glow removed in selected ticks.
	 * <pre>{@code
	 * GlowUtils.glowBlock(block, NamedTextColor.RED, 10);
	 * }</pre>
	 * @param block Target block to glow
	 * @param color color of glowing
	 * @param duration how much ticks to glow
	 */
	public static void glowBlock(@NotNull Block block, @NotNull NamedTextColor color, int duration) {
		Location loc = block.getLocation();
		loc.getWorld().spawn(loc, Shulker.class, s -> {
			s.setAI(false);
			s.setSilent(true);
			s.setInvulnerable(true);
			s.setInvisible(true);
			s.setCollidable(false);
			s.setPeek(0);
			GlowUtils.glow(s, color);
			Bukkit.getScheduler().runTaskLater(XmasLegacy.getInstance(), s::remove, duration);
		});
	}
}
