package org.lazberry.xmaslegacy.huntingZone.CustomMobs;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.KeyUtils;

public interface CustomMob {
    @NotNull MobKey getKey();
    @NotNull LivingEntity spawn(@NotNull Location loc);
    @NotNull MobGrade getGrade();
    default boolean isEntity(LivingEntity e) {
        String value = e.getPersistentDataContainer().get(KeyUtils.get("custom_mobs"), PersistentDataType.STRING);
        return value != null && value.equals(getKey().name());
    }
}
