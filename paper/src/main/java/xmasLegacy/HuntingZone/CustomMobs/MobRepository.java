package xmasLegacy.HuntingZone.CustomMobs;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmasLegacy.HuntingZone.CustomMobs.Boss.AbstractBossMobs;
import xmasLegacy.HuntingZone.CustomMobs.Elite.AbstractEliteMobs;
import xmasLegacy.HuntingZone.CustomMobs.Honored.AbstractHonoredMobs;
import xmasLegacy.HuntingZone.CustomMobs.Mythic.AbstractMythicMobs;
import xmasLegacy.HuntingZone.CustomMobs.Named.AbstractNamedMobs;
import xmasLegacy.HuntingZone.CustomMobs.Unrated.*;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class MobRepository {
    private final @NotNull Map<MobKey, CustomMob> mobInstances = new HashMap<>();
    private final @NotNull IcedZombie icedZombie;
    private final @NotNull HunterZombie hunterZombie;
    private final @NotNull IceCube iceCube;

    private static @Nullable MobRepository instance;

    @Contract(pure = true)
    public static MobRepository getInstance() {
        if (instance == null) instance = new MobRepository();
        return instance;
    }

    private MobRepository() {
        this.icedZombie = IcedZombie.getInstance();
        this.hunterZombie = HunterZombie.getInstance();
        this.iceCube = IceCube.getInstance();
    }

    public void init() {
        this.mobInstances.put(MobKey.ICED_ZOMBIE, icedZombie);
        this.mobInstances.put(MobKey.HUNTER_ZOMBIE, hunterZombie);
        this.mobInstances.put(MobKey.ICE_CUBE, iceCube);
    }

    @CheckReturnValue
    public @NotNull CustomMob[] getMobInstance() {
        return this.mobInstances.values().toArray(CustomMob[]::new);
    }

    @CheckReturnValue
    public @NotNull CustomMob[] getMobInstance(@NotNull MobGrade grade) {
        return switch (grade) {
            case UNRATED -> this.mobInstances.values().stream().filter(i -> i instanceof AbstractUnratedMobs).toArray(CustomMob[]::new);
            case NAMED -> this.mobInstances.values().stream().filter(i -> i instanceof AbstractNamedMobs).toArray(CustomMob[]::new);
            case HONORED -> this.mobInstances.values().stream().filter(i -> i instanceof AbstractHonoredMobs).toArray(CustomMob[]::new);
            case ELITE -> this.mobInstances.values().stream().filter(i -> i instanceof AbstractEliteMobs).toArray(CustomMob[]::new);
            case MYTHIC -> this.mobInstances.values().stream().filter(i -> i instanceof AbstractMythicMobs).toArray(CustomMob[]::new);
            case BOSS -> this.mobInstances.values().stream().filter(i -> i instanceof AbstractBossMobs).toArray(CustomMob[]::new);
        };
    }

    @CheckReturnValue
    public @Nullable CustomMob getMobInstance(@NotNull LivingEntity le) {
        return this.mobInstances.values().stream()
                .filter(i -> i.isEntity(le))
                .findFirst()
                .orElse(null);
    }
}
