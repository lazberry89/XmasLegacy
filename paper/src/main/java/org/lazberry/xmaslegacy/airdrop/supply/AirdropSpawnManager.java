package org.lazberry.xmaslegacy.airdrop.supply;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.lazberry.xmaslegacy.AirdropConfig;
import org.lazberry.xmaslegacy.airdrop.AirdropManager;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Setter @Getter
@Registry.Include(type = ServerType.WILD)
public class AirdropSpawnManager implements Initiator {
    private final AirdropManager airdropManager;
    private final AirdropConfig config;
    private Location center;
    private int spawnRange = 1000;
    private int spawnIntervalTicks = 20 * 60 * 30;

    @Inject
    public AirdropSpawnManager(AirdropManager airdropManager, AirdropConfig config) {
        this.airdropManager = airdropManager;
        this.config = config;
    }

    @Override
    public void init() {
        reload().join();
    }

    public CompletableFuture<Void> reload() {
        return CompletableFuture.runAsync(() -> {
           center = config.getCenterLocation();
           spawnRange = config.getSupplyRange();
           spawnIntervalTicks = config.getSpawnInterval();
        });
    }

    public void dropAirdropRandomly() {
        if (center == null) throw new IllegalStateException("Center location of supply range is not set.");

        Location spawn = getRandomLocation();
        World world = spawn.getWorld();

        world.getChunkAtAsync(spawn).thenAccept(chunk -> {
            var x = spawn.getBlockX();
            var z = spawn.getBlockZ();

            airdropManager.spawnAirDrop(spawn);
            Bukkit.broadcast(ColorUtils.chat(Alert.GREEN + String.format("보급이 낙하중입니다! &6(x: %d, z: %d)", x, z)));
        });
    }

    private Location getRandomLocation() {
        if (center == null) throw new IllegalStateException("Center location of supply range is not set.");

        var x = center.getBlockX();
        var z = center.getBlockZ();

        var minX = x - spawnRange / 2;
        var maxX = x + spawnRange / 2;
        var minZ = z - spawnRange / 2;
        var maxZ = z + spawnRange / 2;

        var resultX = ThreadLocalRandom.current().nextInt(minX, maxX + 1);
        var resultZ = ThreadLocalRandom.current().nextInt(minZ, maxZ + 1);

        var newLoc = center.clone();
        newLoc.setX(resultX);
        newLoc.setZ(resultZ);

        return newLoc;
    }
}
