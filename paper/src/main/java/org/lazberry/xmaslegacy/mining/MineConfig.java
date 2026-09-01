package org.lazberry.xmaslegacy.mining;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.mining.logics.MineManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.AbstractDataProcessor;
import org.lazberry.xmaslegacy.utils.ConfigBuilder;

@Slf4j
@Registry.Include(type = ServerType.MAIN)
public class MineConfig extends AbstractDataProcessor {
    private final MineManager mm;

    @Inject
    public MineConfig(XmasLegacy plugin, MineManager mm) {
        super(plugin, "mine_config");
        this.mm = mm;
    }

    @Override
    public void abstractInitiate() {
    }

    @Override
    public void saveSync() {
        String path = "settings.";
        synchronized (this) {
            @Nullable World world = mm.getWorld();
            var external = mm.getExternalMine();
            var internal = mm.getInternalMine();
            String chancePath = path + "chance.";
            ConfigBuilder.of(getConfig())
                    .set(path + "world", world != null ? world.getName() : "world")
                    .set(path + "locations." + "external." + "loc1", external == null ? null : external.loc1())
                    .set(path + "locations." + "external." + "loc2", external == null ? null : external.loc2())
                    .set(path + "locations." + "internal." + "loc1", internal == null ? null : internal.loc1())
                    .set(path + "locations." + "internal." + "loc2", internal == null ? null : internal.loc2())
                    .set(chancePath + "emerald", mm.getChanceOfEmerald())
                    .set(chancePath + "diamond", mm.getChanceOfDiamond())
                    .set(chancePath + "lapis", mm.getChanceOfLazuli())
                    .set(chancePath + "gold", mm.getChanceOfGold())
                    .set(chancePath + "iron", mm.getChanceOfIron())
                    .set(chancePath + "coal", mm.getChanceOfCoal())
                    .set(chancePath + "stone", mm.getChanceOfStone())
                    .save(getFile());
        }
    }

    @Override
    public void loadSync() {
        synchronized (this) {
            var builder = ConfigBuilder.of(getConfig());
            String path = "settings.";

            String worldName = builder.getValue(path + "world", "world");
            World world = Bukkit.getWorld(worldName);
            if (world != null) {
                mm.setWorld(world);
            }
            Location internalLoc1 = builder.getValue(path + "locations.internal.loc1", Location.class);
            Location internalLoc2 = builder.getValue(path + "locations.internal.loc2", Location.class);
            if (internalLoc1 != null && internalLoc2 != null) {
                mm.registerInternal(new MineField(internalLoc1, internalLoc2));
            }

            Location externalLoc1 = builder.getValue(path + "locations.external.loc1", Location.class);
            Location externalLoc2 = builder.getValue(path + "locations.external.loc2", Location.class);
            if (externalLoc1 != null && externalLoc2 != null) {
                mm.registerExternal(new MineField(externalLoc1, externalLoc2));
            }


            String chancePath = path + "chance.";
            mm.setChanceOfEmerald(builder.getValue(chancePath + "emerald", mm.getChanceOfEmerald()));
            mm.setChanceOfDiamond(builder.getValue(chancePath + "diamond", mm.getChanceOfDiamond()));
            mm.setChanceOfLazuli(builder.getValue(chancePath + "lapis", mm.getChanceOfLazuli()));
            mm.setChanceOfGold(builder.getValue(chancePath + "gold", mm.getChanceOfGold()));
            mm.setChanceOfIron(builder.getValue(chancePath + "iron", mm.getChanceOfIron()));
            mm.setChanceOfCoal(builder.getValue(chancePath + "coal", mm.getChanceOfCoal()));
            mm.setChanceOfStone(builder.getValue(chancePath + "stone", mm.getChanceOfStone()));
        }
    }
}
