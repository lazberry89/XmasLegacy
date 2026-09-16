package org.lazberry.xmaslegacy.versus;

import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Registry.Include(type = ServerType.MAIN)
public class VersusManager {
    private final XmasLegacy plugin;

    @Inject
    public VersusManager(XmasLegacy plugin) {
        this.plugin = plugin;
    }


}
