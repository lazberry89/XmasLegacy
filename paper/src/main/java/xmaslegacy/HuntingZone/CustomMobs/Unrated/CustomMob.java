package xmaslegacy.HuntingZone.CustomMobs.Unrated;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.HuntingZone.CustomMobs.MobGrade;
import xmaslegacy.HuntingZone.CustomMobs.MobKey;

public interface CustomMob {
    boolean isEntity(LivingEntity e);
    @NotNull MobKey getKey();
    @NotNull LivingEntity spawn(@NotNull Location loc);
    @NotNull MobGrade getGrade();
}
