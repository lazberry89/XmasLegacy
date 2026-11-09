package org.lazberry.xmaslegacy.sin;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.GlowUtils;

@Slf4j
@Components.Include(type = { ServerType.MAIN, ServerType.WILD })
public class NightmareManager implements Initiator {
	private final XmasLegacy plugin;
	private final Team team;

	@Getter
	@Setter
	private String hiddenName = "&c&k###";

	@Inject
	public NightmareManager(XmasLegacy plugin) {
		this.plugin = plugin;
		this.team = createTeam();
	}

	@Override
	public void init() {
	}

	public void blackPlayer(Player player) {
		var name = player.getName();
		if (!team.hasEntry(name))
			team.addEntry(name);

		hidePlayerName(player);
		player.setInvisible(true);
		GlowUtils.glow(player, NamedTextColor.BLACK);
	}

	public boolean hasBackup(Player player) {
		return team.hasEntry(player.getName());
	}

	public void cleanup(Player player) {
		team.removeEntry(player.getName());
		resetPlayer(player);
	}

	public void resetPlayer(Player player) {
		var name = player.getName();
		if (team.hasEntry(name))
			team.removeEntry(name);

		player.setInvisible(false);
		GlowUtils.clearGlow(player);
		showPlayerName(player);
	}

	private Team createTeam() {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

		Team team = board.getTeam("hide_tags");
		if (team == null) {
			team = board.registerNewTeam("hide_tags");
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
			team.setAllowFriendlyFire(true);
			team.setCanSeeFriendlyInvisibles(false);
		}
		return team;
	}

	private void hidePlayerName(Player player) {
		player.displayName(ColorUtils.chat(hiddenName));
		player.playerListName(ColorUtils.chat(hiddenName));
	}

	private void showPlayerName(Player player) {
		player.displayName(null);
		player.playerListName(null);
	}

	private void unregisterTeam() {
		Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

		Team team = board.getTeam("hide_tags");
		if (team != null)
			team.unregister();
	}

	@Override
	public void close() {

		unregisterTeam();
	}
}
