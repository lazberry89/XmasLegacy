package org.lazberry.xmaslegacy.versus;

import lombok.Data;
import org.bukkit.Location;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Data
@ConsumableClass
public class VersusField {
    private final String id;
    private final Location fighterSpawn1;
    private final Location fighterSpawn2;
    private final Set<Location> spectatorSpawn = ConcurrentHashMap.newKeySet();
    private UUID redFighter;
    private UUID blueFighter;
    private boolean running;

    public VersusField(String id, Location fighterSpawn1, Location fighterSpawn2) {
        this.id = id;
        this.fighterSpawn1 = fighterSpawn1;
        this.fighterSpawn2 = fighterSpawn2;
    }


}
