package org.lazberry.xmaslegacy.rank.playtime;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;

import java.util.UUID;

@Registry.Exclude(type = ServerType.LOBBY)
public class UserPlaytimeManager {
    private final UserManager userManager;

    @Inject
    public UserPlaytimeManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void updateUserPlaytime(UUID uuid) {
        var player = Bukkit.getPlayer(uuid);
        User user = userManager.getProxyUser(uuid);

        if (player == null || user == null) return;

        String server = JedisService.getUserServer(uuid);
        if (server == null) return;

        long playtime = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        JedisService.setToJedis("uuid:" + uuid + ":server:" + server, String.valueOf(playtime));

        userManager.editProxyUser(uuid,
                u -> u.setPlayTime(getUserPlaytime(uuid)));
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
