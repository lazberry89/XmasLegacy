package org.lazberry.xmaslegacy.collectors.hunter;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.utils.KeyUtils;

public interface Hunter {
	default NamespacedKey key() {return KeyUtils.get("hunter");}
    void attack(Player target);
    LivingEntity spawn(Location loc);
}
