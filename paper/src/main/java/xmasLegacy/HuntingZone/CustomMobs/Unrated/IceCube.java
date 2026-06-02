package xmasLegacy.HuntingZone.CustomMobs.Unrated;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmasLegacy.HuntingZone.CustomMobs.MobKey;
import xmasLegacy.Utils.GlowUtils;

@SuppressWarnings("unused")
public class IceCube extends AbstractUnratedMobs implements CustomMob, UnratedMob {
    private final @NotNull MobKey key;
    private static @Nullable IceCube instance;

    @Contract(pure = true)
    public static IceCube getInstance() {
        if (instance == null) instance = new IceCube();
        return instance;
    }

    @ApiStatus.Internal
    private IceCube() {
        super();
        this.key = MobKey.ICE_CUBE;
    }

    @Override
    public @NotNull MobKey getKey() {
        return this.key;
    }

    @Override
    public @NotNull LivingEntity spawn(@NotNull Location loc) {
        return loc.getWorld().spawn(loc, MagmaCube.class, m -> {
            GlowUtils.setGlowColor(m, getGrade().color());
            m.setWander(false);
            m.setSize(2);
        });
    }

    @Override
    public void attack(LivingEntity target) {}
}
