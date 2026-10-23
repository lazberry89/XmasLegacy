package org.lazberry.xmaslegacy.sin;

import com.destroystokyo.paper.profile.PlayerProfile;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Registry;

import java.net.URI;
import java.net.URL;

@Slf4j
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class NightmareManager implements Initiator {
    private final XmasLegacy plugin;
    private final Team team;

    public NightmareManager(XmasLegacy plugin) {
        this.plugin = plugin;
        this.team = createTeam();
    }

    @Override
    public void init() {

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

    private void unregisterTeam() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();

        Team team = board.getTeam("hide_tags");
        if (team != null) team.unregister();
    }

    private void updatePlayerSkin(Player p) {
        try {
            PlayerProfile profile = p.getPlayerProfile();

            PlayerTextures textures = profile.getTextures();

            URL uri = URI.create().toURL();

            profile.setTextures();
        } catch (Exception e) {
            log.error("Failed to apply skin to target player ({})", p.getName());
        }
    }

    public void nightmarePlayer(Player player) {
        team.addPlayer(player);
    }

    @Override
    public void close() {
        unregisterTeam();
    }
}
