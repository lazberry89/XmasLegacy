package org.lazberry.xmaslegacy.collectors.hunter.horror;

import lombok.NoArgsConstructor;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.collectors.hunter.Hunter;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.concurrent.ThreadLocalRandom;

@NoArgsConstructor
public class Silence implements Hunter {
    private static final NamespacedKey rage = KeyUtils.get("rage");

    @Override
    public void attack(Player target) {

    }

    public static boolean isEntity(Entity entity) {
        return entity instanceof Warden w &&
                w.getPersistentDataContainer().has(KeyUtils.get(Difficulty.HORROR.name() + "_hunter"));
    }

    public static boolean isFurious(Entity entity) {
        return entity instanceof Warden w &&
                Boolean.TRUE.equals(w.getPersistentDataContainer().get(rage, PersistentDataType.BOOLEAN));
    }

    @Override
    public LivingEntity spawn(Location loc) {
        return loc.getWorld().spawn(loc, Warden.class, w -> {
            GlowUtils.glow(w, NamedTextColor.RED);
            KeyUtils.set(w, key(Difficulty.HORROR), true);
            w.setInvulnerable(true);
            w.setArrowsInBody(ThreadLocalRandom.current().nextInt(5, 11));
        });
    }
}
