package org.lazberry.xmaslegacy.user;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Blocking;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.FloodgateUtils;

@Slf4j
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
            try {
                var uuid = p.getUniqueId();
                var name = p.getName();
                boolean isFloodgate = false;
                try {
                    isFloodgate = FloodgateUtils.isFloodgate(uuid);
                } catch (Throwable ignored) {}
                userSaveManager.load(uuid, name, isFloodgate);
                log.info("Loaded user info for online player {} ({}) on reload.", name, uuid);
            } catch (Exception e) {
                log.error("Failed to load user info for player {} on reload", p.getName(), e);
            }
        });
    }

    @Override
    public void close() {
        userSaveManager.saveAll();
        userSaveManager.clearCache();
    }
}
