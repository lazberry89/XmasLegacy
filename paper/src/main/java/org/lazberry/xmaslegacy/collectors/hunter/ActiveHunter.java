package org.lazberry.xmaslegacy.collectors.hunter;

import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

public record ActiveHunter(Hunter hunter, LivingEntity entity, BukkitTask task) {
}
