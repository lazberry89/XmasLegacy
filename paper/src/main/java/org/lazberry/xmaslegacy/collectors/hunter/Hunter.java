package org.lazberry.xmaslegacy.collectors.hunter;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public interface Hunter {
    void attack(Player target);
    LivingEntity spawn(Location loc);
}
