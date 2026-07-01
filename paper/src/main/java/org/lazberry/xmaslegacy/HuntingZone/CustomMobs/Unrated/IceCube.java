package org.lazberry.xmaslegacy.HuntingZone.CustomMobs.Unrated;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.HuntingZone.CustomMobs.MobKey;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.Utils.KeyUtils;

public class IceCube extends AbstractUnratedMobs implements CustomMob, UnratedMob {
    private final @NotNull MobKey key;

    public IceCube() {
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
            m.getPersistentDataContainer().set(KeyUtils.get("custom_mobs"), PersistentDataType.STRING, this.key.name());
        });
    }

    @Override
    public void attack(LivingEntity target) {}
}
