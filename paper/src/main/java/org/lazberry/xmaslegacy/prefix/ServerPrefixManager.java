package org.lazberry.xmaslegacy.prefix;

import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

@Components.Exclude(type = ServerType.LOBBY)
public class ServerPrefixManager {
    private final CommonPrefixManager prefixManager;
    private final XmasLegacy plugin;

    @Inject
    public ServerPrefixManager(CommonPrefixManager prefixManager, XmasLegacy plugin) {
        this.prefixManager = prefixManager;
        this.plugin = plugin;
    }


}
