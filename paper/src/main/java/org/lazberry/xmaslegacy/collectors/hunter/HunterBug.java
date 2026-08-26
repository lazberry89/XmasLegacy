package org.lazberry.xmaslegacy.collectors.hunter;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.concurrent.ThreadLocalRandom;

public class HunterBug implements Hunter {
    private final HunterData data;

    public HunterBug(HunterData data) {
        this.data = data;
    }

    @Override
    public void attack(Player target) {
		if (ThreadLocalRandom.current().nextDouble() < data.hunterBug().getDebuffChance()) {
			target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 10, 1, true, false, false));
		}
    }

    @Override
    public LivingEntity spawn(Location loc) {
        HunterData.HunterBug bugData = data.hunterBug();

        double random = ThreadLocalRandom.current().nextDouble();
        EntityType type = (random < bugData.getHunter1chance())
                ? bugData.getHunterType1()
                : bugData.getHunterType2();
        Class<? extends Entity> entityClass = type.getEntityClass();
        if (entityClass == null) throw new IllegalArgumentException("Class not found on Config object.");

        Class<? extends LivingEntity> lv = type.getEntityClass().asSubclass(LivingEntity.class);
        return loc.getWorld().spawn(loc, lv, b -> {
            GlowUtils.glow(b, NamedTextColor.GRAY);
            KeyUtils.set(b, key(), "HUNTER_BUG");
        });
    }
}
