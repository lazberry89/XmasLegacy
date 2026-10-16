package org.lazberry.xmaslegacy.temperature;

import org.bukkit.event.Listener;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class WarmthListener implements Listener {
    private final WarmthManager wm;

    @Inject
    public WarmthListener(WarmthManager wm) {
        this.wm = wm;
    }
}
