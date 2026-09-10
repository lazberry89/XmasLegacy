package org.lazberry.xmaslegacy.system.temperature;

import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class WarmthListener {
    private final WarmthManager wm;

    @Inject
    public WarmthListener(WarmthManager wm) {
        this.wm = wm;
    }
}
