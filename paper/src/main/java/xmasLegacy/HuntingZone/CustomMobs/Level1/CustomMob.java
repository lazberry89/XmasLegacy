package xmasLegacy.HuntingZone.CustomMobs.Level1;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import xmasLegacy.HuntingZone.CustomMobs.MobGrade;
import xmasLegacy.HuntingZone.CustomMobs.MobKey;

public interface CustomMob {
    @NotNull MobKey getKey();
    @NotNull MobGrade getGrade();
    @NotNull LivingEntity spawn(@NotNull Location loc);
}
