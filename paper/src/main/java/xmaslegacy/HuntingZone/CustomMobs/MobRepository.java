package xmaslegacy.HuntingZone.CustomMobs;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmaslegacy.HuntingZone.CustomMobs.Boss.AbstractBossMobs;
import xmaslegacy.HuntingZone.CustomMobs.Elite.AbstractEliteMobs;
import xmaslegacy.HuntingZone.CustomMobs.Honored.AbstractHonoredMobs;
import xmaslegacy.HuntingZone.CustomMobs.Mythic.AbstractMythicMobs;
import xmaslegacy.HuntingZone.CustomMobs.Named.AbstractNamedMobs;
import xmaslegacy.HuntingZone.CustomMobs.Unrated.*;
import xmaslegacy.HuntingZone.ZoneType;

import java.util.HashMap;
import java.util.Map;

public enum MobRepository {
	INSTANCE;

    private final @NotNull Map<MobKey, CustomMob> mobInstances = new HashMap<>();
    private final @NotNull IcedZombie icedZombie;
    private final @NotNull HunterZombie hunterZombie;
    private final @NotNull IceCube iceCube;

    MobRepository() {
        this.icedZombie = new IcedZombie();
        this.hunterZombie = new HunterZombie();
        this.iceCube = new IceCube();
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
            case UNRATED -> this.mobInstances.values().stream().filter(u -> u instanceof AbstractUnratedMobs).toArray(CustomMob[]::new);
            case NAMED -> this.mobInstances.values().stream().filter(n -> n instanceof AbstractNamedMobs).toArray(CustomMob[]::new);
            case HONORED -> this.mobInstances.values().stream().filter(h -> h instanceof AbstractHonoredMobs).toArray(CustomMob[]::new);
            case ELITE -> this.mobInstances.values().stream().filter(e -> e instanceof AbstractEliteMobs).toArray(CustomMob[]::new);
            case MYTHIC -> this.mobInstances.values().stream().filter(m -> m instanceof AbstractMythicMobs).toArray(CustomMob[]::new);
            case BOSS -> this.mobInstances.values().stream().filter(b -> b instanceof AbstractBossMobs).toArray(CustomMob[]::new);
        };
    }

    @CheckReturnValue
    public @Nullable CustomMob getMobInstance(@NotNull LivingEntity le) {
        return this.mobInstances.values().stream()
                .filter(i -> i.isEntity(le))
                .findFirst()
                .orElse(null);
    }

	@CheckReturnValue
	public <C extends @NotNull CustomMob> @Nullable C getMobInstance(@NotNull MobKey key, @NotNull Class<C> clazz) {
        var mob = this.mobInstances.get(key);
        return mob != null ? clazz.cast(mob) : null;
	}

	public @NotNull CustomMob[] getMobInstance(@NotNull ZoneType type) {
		return switch (type) {
			case ICE_STAGE ->
				new CustomMob[] {
						this.icedZombie,
						this.hunterZombie,
						this.iceCube
				};
			case SKY_GARDEN -> new CustomMob[] {};
			case SOUL_GRAVEYARD -> new CustomMob[] {};
		};
	}
}
