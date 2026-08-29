package org.lazberry.xmaslegacy.collectors.hunter.horror;

import org.bukkit.Location;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.collectors.game.CollectorsManager;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.collectors.hunter.HunterHandler;
import org.lazberry.xmaslegacy.collectors.hunter.HunterRepository;

public class SilenceHandler extends HunterHandler {
    private static final long RAGE_DURATION_TICK = 600L;
    private final XmasLegacy plugin;

    public SilenceHandler(CollectorsManager cm, HunterRepository repository, XmasLegacy plugin) {
        super(cm, repository, Difficulty.HORROR);
        this.plugin = plugin;
    }

    @Override
    public void init() {

    }

    @Override
    public void close() {
        super.close();
    }

    @Override
    public boolean spawnHunter(Location location) {
        return false;
    }

    @Override
    protected void removeHunters() {

    }
}
