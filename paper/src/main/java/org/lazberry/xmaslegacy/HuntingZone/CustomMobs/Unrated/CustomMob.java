package org.lazberry.xmaslegacy.HuntingZone.CustomMobs.Unrated;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.HuntingZone.CustomMobs.MobGrade;
import org.lazberry.xmaslegacy.HuntingZone.CustomMobs.MobKey;

public interface CustomMob {
    boolean isEntity(LivingEntity e);
    @NotNull MobKey getKey();
    @NotNull LivingEntity spawn(@NotNull Location loc);
    @NotNull MobGrade getGrade();
}
