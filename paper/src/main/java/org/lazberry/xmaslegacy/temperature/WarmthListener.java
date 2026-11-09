package org.lazberry.xmaslegacy.temperature;

import org.bukkit.event.Listener;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;

@Listeners
@Components.Exclude(type = ServerType.LOBBY)
public class WarmthListener implements Listener {
    private final WarmthManager wm;

    @Inject
    public WarmthListener(WarmthManager wm) {
        this.wm = wm;
    }
}
