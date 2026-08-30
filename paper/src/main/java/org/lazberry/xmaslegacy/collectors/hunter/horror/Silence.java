package org.lazberry.xmaslegacy.collectors.hunter.horror;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.collectors.hunter.Hunter;
import org.lazberry.xmaslegacy.collectors.hunter.HunterData;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.concurrent.ThreadLocalRandom;

public class Silence implements Hunter {
    private static final NamespacedKey rage = KeyUtils.get("rage");
    private static final NamespacedKey chase = KeyUtils.get("chase");
    private final HunterData data;

    public Silence(HunterData data) {
        this.data = data;
    }

    @Override
    public void attack(Player target) {

    }

	public static boolean isEntity(Entity entity) {
		return entity instanceof Warden w &&
				w.getPersistentDataContainer().has(KeyUtils.get(Difficulty.HORROR.name() + "_hunter"));
	}

	public static boolean isFurious(Entity entity) {
		return isEntity(entity) &&
				Boolean.TRUE.equals(entity.getPersistentDataContainer().get(rage, PersistentDataType.BOOLEAN));
	}

	public static void setFurious(Entity entity, boolean furious) {
		if (!isEntity(entity)) return;
		if (isFurious(entity) == furious) return;

		KeyUtils.set(entity, rage, furious);
	}

    public static boolean isChasing(Entity entity) {
        return isEntity(entity) &&
                Boolean.TRUE.equals(entity.getPersistentDataContainer().get(chase, PersistentDataType.BOOLEAN));
    }

    public static void setChasing(Entity entity, boolean chasing) {
        if (!isEntity(entity)) return;
        KeyUtils.set(entity, chase, chasing);
    }

    @Override
    public LivingEntity spawn(Location loc) {
        return loc.getWorld().spawn(loc, Warden.class, w -> {
            GlowUtils.glow(w, NamedTextColor.RED);
            KeyUtils.set(w, key(Difficulty.HORROR), true);
            KeyUtils.set(w, chase, false);
            w.setInvulnerable(true);
            w.setArrowsInBody(ThreadLocalRandom.current().nextInt(5, 11));
            AttributeInstance attack = w.getAttribute(Attribute.ATTACK_DAMAGE);
            if (attack != null) attack.setBaseValue(50);
        });
    }
}
