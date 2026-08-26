package org.lazberry.xmaslegacy.collectors.hunter;

import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

@NoArgsConstructor
public class Silence implements Hunter {

    @Override
    public void attack(Player target) {

    }

    @Override
    public LivingEntity spawn(Location loc) {
        return null;
    }
}
