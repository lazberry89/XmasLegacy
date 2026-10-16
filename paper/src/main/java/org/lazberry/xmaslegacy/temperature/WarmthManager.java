package org.lazberry.xmaslegacy.temperature;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
@Registry.Exclude(type = ServerType.LOBBY)
public class WarmthManager implements Initiator {
    private final Map<Long, WarmthZone> zones = new ConcurrentHashMap<>();
    private final WarmthZoneConfig zoneConfig;

    private volatile double bonusChance = 0.2;
    private volatile double maxRegenerateSum = 10.0;

    @Inject
    public WarmthManager(WarmthZoneConfig zoneConfig) {
        this.zoneConfig = zoneConfig;
    }

    @Override
    public void init() {
        zones.clear();
        zones.putAll(zoneConfig.loadSync(bonusChance, maxRegenerateSum));
    }

    public Collection<WarmthZone> getZones() {
        return Collections.unmodifiableCollection(zones.values());
    }

    public WarmthZone getWarmthZone(Location loc) {
        var id = loc.getChunk().getChunkKey();
        return zones.get(id);
    }

    public WarmthZone getWarmthZone(Chunk chunk) {
        return zones.get(chunk.getChunkKey());
    }

    public void createWarmthZone(Chunk chunk, WarmthLevel level) {
        zones.computeIfAbsent(chunk.getChunkKey(), i -> new WarmthZone(chunk, level, bonusChance, maxRegenerateSum));
    }

    public void removeWarmthZone(long id) {
        zones.remove(id);
    }

    @Override
    public void close() {
        zoneConfig.saveSync(getZones());
        zones.clear();
    }
}
