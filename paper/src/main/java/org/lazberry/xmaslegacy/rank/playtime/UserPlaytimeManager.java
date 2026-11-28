package org.lazberry.xmaslegacy.rank.playtime;

import org.lazberry.xmaslegacy.utils.ServerUtils;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;

import java.util.UUID;

@Components.Exclude(type = ServerType.LOBBY)
public class UserPlaytimeManager {
    private final UserManager userManager;
    private final XmasLegacy plugin;

    @Inject
    public UserPlaytimeManager(UserManager userManager, XmasLegacy plugin) {
        this.userManager = userManager;
        this.plugin = plugin;
    }

    public void updateUserPlaytime(UUID uuid) {
        var player = Bukkit.getPlayer(uuid);
        // The loaded user, not its Redis copy: saving the loaded one would overwrite the copy with the old playtime.
        User user = userManager.getUser(uuid);

        if (player == null || user == null) return;

        ServerType server = ServerUtils.getServerType(plugin);

        long playtime = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        JedisService.setToJedis("uuid:" + uuid + ":server:" + server, String.valueOf(playtime));

        user.setPlayTime(getUserPlaytime(uuid));
    }

    public long getUserPlaytime(UUID uuid) {
        long result = 0;
        for (var server : ServerType.getUserValidServers()) {
            String value = JedisService.getFromJedis("uuid:" + uuid + ":server:" + server);
            long playTimeByServer;
            try {
                playTimeByServer = Long.parseLong(value);
            } catch (NumberFormatException e) {
                playTimeByServer = 0;
            }
            result += playTimeByServer;
        }
        return result;
    }
}
