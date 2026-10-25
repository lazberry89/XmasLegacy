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

	private static final java.util.Map<java.util.UUID, NamedTextColor> ACTIVE_GLOWS = new java.util.concurrent.ConcurrentHashMap<>();

	private static @NotNull String getTeamName(@NotNull NamedTextColor color) {
		String name = "gl_" + color.toString().toLowerCase();
		return name.length() > 16 ? name.substring(0, 16) : name;
	}

	private static void applyToScoreboard(@NotNull Scoreboard scoreboard, @NotNull Entity entity, @NotNull NamedTextColor color) {
		String teamName = getTeamName(color);
		Team team = scoreboard.getTeam(teamName);

		if (team == null) {
			team = scoreboard.registerNewTeam(teamName);
			team.color(color);
		}

		team.addEntity(entity);
	}

	private static void removeFromScoreboard(@NotNull Scoreboard scoreboard, @NotNull Entity entity) {
		Team team = scoreboard.getEntityTeam(entity);
		if (team != null && team.getName().startsWith("gl_")) {
			team.removeEntity(entity);
		}
	}

	/**
	 * Sync currently active glowing entities to a target player/custom scoreboard.
	 * Call this when a player receives a newly constructed scoreboard.
	 * @param scoreboard Target scoreboard to register active glowing entities into.
	 */
	public static void syncGlowsToScoreboard(@NotNull Scoreboard scoreboard) {
		ACTIVE_GLOWS.forEach((uuid, color) -> {
			Entity e = Bukkit.getEntity(uuid);
			if (e != null && e.isValid()) {
				applyToScoreboard(scoreboard, e, color);
			}
		});
	}

	/**
	 * This method glows target entity with param2 color.
	 * Also apply team to entity across all online player scoreboards and main scoreboard.
	 * <pre>{@code
	 * GlowUtils.glow(target, NamedTextColor.YELLOW);
	 * }</pre>
	 * @param entity Target entity to give glow.
	 * @param color target color to glow.
	 */
	public static void glow(@NotNull Entity entity, @NotNull NamedTextColor color) {
		ACTIVE_GLOWS.put(entity.getUniqueId(), color);

		applyToScoreboard(Bukkit.getScoreboardManager().getMainScoreboard(), entity, color);
		for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
			Scoreboard sb = p.getScoreboard();
			if (sb != Bukkit.getScoreboardManager().getMainScoreboard()) {
				applyToScoreboard(sb, entity, color);
			}
		}

		entity.setGlowing(true);
	}

	/**
	 * Remove target entity's glowing. Also remove team entry from all player scoreboards and main scoreboard.
	 * <pre>{@code
	 * GlowUtils.clearGlow(target);
	 * }</pre>
	 * @param entity target entity.
	 */
	public static void clearGlow(@NotNull Entity entity) {
		ACTIVE_GLOWS.remove(entity.getUniqueId());
		entity.setGlowing(false);

		removeFromScoreboard(Bukkit.getScoreboardManager().getMainScoreboard(), entity);
		for (org.bukkit.entity.Player p : Bukkit.getOnlinePlayers()) {
			Scoreboard sb = p.getScoreboard();
			if (sb != Bukkit.getScoreboardManager().getMainScoreboard()) {
				removeFromScoreboard(sb, entity);
			}
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
