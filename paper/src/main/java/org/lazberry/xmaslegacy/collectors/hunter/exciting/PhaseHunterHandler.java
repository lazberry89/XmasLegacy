package org.lazberry.xmaslegacy.collectors.hunter.exciting;

import org.bukkit.Location;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.collectors.game.CollectorsManager;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.collectors.hunter.HunterHandler;
import org.lazberry.xmaslegacy.collectors.hunter.HunterRepository;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PhaseHunterHandler extends HunterHandler {
    private final Set<UUID> hunters = ConcurrentHashMap.newKeySet(5);
    private final XmasLegacy plugin;

    @Inject
    public PhaseHunterHandler(CollectorsManager cm, HunterRepository repository, XmasLegacy plugin) {
        super(cm, repository, Difficulty.EXCITING);
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
        return whenSpawnAvailable((s, h) -> {
            UUID uuid = h.spawn(location).getUniqueId();
            hunters.add(uuid);
            s.addHunter(uuid);
        }, () -> {});
    }

    @Override
    protected void removeHunters() {

    }
}
