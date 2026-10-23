package org.lazberry.xmaslegacy.sin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class NightmareManager implements Initiator {
	private static final String NIGHTMARE_SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTc5Mjc3MDA0NzQ2OSwKICAicHJvZmlsZUlkIiA6ICI2YTQ3NjRhZWEwNWY0MTE1OTc0NzFlZjNjYWU4ZTdmZiIsCiAgInByb2Zpb" +
			"GVOYW1lIiA6ICJiYWRlZW5kamVoIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk5ZTE5YzZmMTk4MjVlMDQ3NzBmOTZkMTVmZDM1YzJjYjM3OWUyMjJhZTMwNTk2YjE5NjhiYjM1NDFiMzRjMDEiCiAgICB9CiAgfQp9";
	private static final String NIGHTMARE_SKIN_SIGNATURE = "OeHuNPzwVXio/MY9xx4WbFq2m1UuK0XwzD5S+3ThVqUQUUK5cby1xRhEPKfr20Y0XrzoiR5cIzj/TQlj2RQuVUnCEGvO22p29AcQfWnrCmWn5CUJwfMxdzB+QXxYhkha1P0J0GM9VOaGzTqU6TSXWTPkAMkmgm0gYxB/uzFD559EJc6RwRqPFygv7zixW35WyRYqV423ALHnNlEKwcmMze1AhbWOvkS9On1" +
			"/9Pss/uEYUorecm0LxIXo9fqH2fCJV1xYpS+cF1X4z6Olkyuvbpc9Jw25B5EUcJLHKTMOP1N+63B0yoc8eVJKtG8y8yuX0to2OIM2XkddEhs5hFXTpbPwOgMnXXz6b2vUrpfsbxEgKsEcTT7qi3UD6lOThA30KkfBnULlz9RMst21IuByyskkVTi9tbQ/+pODBnGqu6uLTccZhVw6wEQff5EmvZojZDi+QRkqkQBOaqzlHxTb3N7LvJml+sdocs+d5t60QFAv/qnGIt0RUxm4Qh2s+x39upcz9hSshVf0PNuoFVFenfMUSfhv0wgsTahNEkzra2mykm41hAVV97zCVOLAMsyvfuCnXdZ0PjLiJv0O3ftE5Rbum" +
			"+dWjsaRBpoDyWmES0LZgPjCJxjr6G0VIeZVPKdwTHc06arG+T+6dnOtz3x7JuO9B59ldxVxyuDPITpJRaA=";

	private final Map<UUID, ProfileProperty> originalSkinCache = new HashMap<>();
    private final XmasLegacy plugin;
    private final Team team;

	@Getter @Setter
	private String hiddenName = "&c&k###";

	@Inject
    public NightmareManager(XmasLegacy plugin) {
        this.plugin = plugin;
        this.team = createTeam();
    }

    @Override
    public void init() {
		originalSkinCache.clear();
    }

	public void cleanup(Player player) {
		team.removeEntry(player.getName());
		originalSkinCache.remove(player.getUniqueId());
		resetPlayer(player);
	}

	public void cacheOriginalSkin(Player player) {
		PlayerProfile profile = player.getPlayerProfile();
		for (ProfileProperty property : profile.getProperties()) {
			if ("textures".equals(property.getName())) {
				if (NIGHTMARE_SKIN_VALUE.equals(property.getValue())) return;
				originalSkinCache.put(player.getUniqueId(), property);
				break;
			}
		}
	}

	public void blackPlayer(Player player) {
		var name = player.getName();
		if (!team.hasEntry(name)) team.addEntry(name);

		if (!originalSkinCache.containsKey(player.getUniqueId())) {
			cacheOriginalSkin(player);
		}

		hidePlayerName(player);
		applySkinProperty(player, NIGHTMARE_SKIN_VALUE, NIGHTMARE_SKIN_SIGNATURE);
	}

	public boolean hasBackup(Player player) {
		return originalSkinCache.containsKey(player.getUniqueId());
	}

	public void resetPlayer(Player player) {
		var name = player.getName();
		if (team.hasEntry(name)) team.removeEntry(name);

		showPlayerName(player);

		ProfileProperty original = originalSkinCache.get(player.getUniqueId());
		if (original != null) {
			applySkinProperty(player, original.getValue(), original.getSignature());
		} else restoreFromMojang(player);
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
        if (team != null) team.unregister();
    }

	private void applySkinProperty(Player player, String value, String signature) {
		try {
			PlayerProfile profile = player.getPlayerProfile();

			profile.removeProperty("textures");
			profile.setProperty(new ProfileProperty("textures", value, signature));

			player.setPlayerProfile(profile);
			refreshView(player);
		} catch (Exception e) {
			log.error("Failed to apply skin property to target player ({})", player.getName(), e);
		}
	}

	public void refreshView(Player player) {
		for (Player online : Bukkit.getOnlinePlayers()) {
			if (online.equals(player)) continue;

			online.hidePlayer(plugin, player);

			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				if (online.isOnline() && player.isOnline()) {
					online.showPlayer(plugin, player);
				}
			}, 1L);
		}
	}

	public void restoreFromMojang(Player player) {
		PlayerProfile fresh = Bukkit.createProfile(player.getUniqueId(), player.getName());
		fresh.update().thenAcceptAsync(updated -> {
			if (!player.isOnline() || !updated.hasTextures()) return;
			player.setPlayerProfile(updated);
		}, runnable -> Bukkit.getScheduler().runTask(plugin, runnable));
	}

    @Override
    public void close() {
        unregisterTeam();
		originalSkinCache.keySet().stream()
				.map(Bukkit::getPlayer)
				.filter(Objects::nonNull)
				.filter(Player::isValid)
				.forEach(this::cleanup);
		originalSkinCache.clear();
    }
}
