package org.lazberry.xmaslegacy.user;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Blocking;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.FloodgateUtils;

@Registry.Exclude(type = ServerType.LOBBY)
public class UserAdditionalSaveManager implements Initiator {
    private final UserSaveManager userSaveManager;

    @Inject
    public UserAdditionalSaveManager(UserSaveManager userSaveManager) {
        this.userSaveManager = userSaveManager;
    }

    @Override
    public void init() {
        userSaveManager.clearCache();
        loadAll();
    }

    @Blocking
    private void loadAll() {
        Bukkit.getOnlinePlayers().forEach(p -> {
            var uuid = p.getUniqueId();
            var name = p.getName();
            boolean isFloodgate = FloodgateUtils.isFloodgate(uuid);
            userSaveManager.load(uuid, name, isFloodgate);
        });
    }

    @Override
    public void close() {
        userSaveManager.saveAll();
        userSaveManager.clearCache();
	    JedisService.closeJedis();
    }
}
