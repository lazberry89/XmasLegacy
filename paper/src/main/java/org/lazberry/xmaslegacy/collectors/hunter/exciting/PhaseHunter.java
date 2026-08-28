package org.lazberry.xmaslegacy.collectors.hunter.exciting;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.collectors.hunter.Hunter;
import org.lazberry.xmaslegacy.collectors.hunter.HunterData;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

public record PhaseHunter(HunterData data) implements Hunter {
    private static final NamespacedKey key = KeyUtils.get("phase");

    @Override
    public void attack(Player target) {

    }

    public void phase(WitherSkeleton hunter) {
        if (!isEntity(hunter) || isPhase(hunter)) return;
        GlowUtils.clearGlow(hunter);
        KeyUtils.set(hunter, key, true);
        hunter.getEquipment().clear();
        hunter.setSilent(true);
        hunter.setInvisible(true);
    }

    public void dephase(WitherSkeleton hunter) {
        if (!isEntity(hunter) || !isPhase(hunter)) return;
        KeyUtils.set(hunter, key, false);
        GlowUtils.glow(hunter, NamedTextColor.BLACK);
        hunter.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
        hunter.setRemoveWhenFarAway(false);
        hunter.setInvisible(false);
    }

    public static boolean isEntity(Entity entity) {
        return entity instanceof WitherSkeleton lv &&
                lv.getPersistentDataContainer().has(KeyUtils.get(Difficulty.EXCITING.name() + "_hunter"));
    }

    public static boolean isPhase(WitherSkeleton skeleton) {
        return isEntity(skeleton)
                && Boolean.TRUE.equals(skeleton.getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN));
    }

    @Override
    public LivingEntity spawn(Location loc) {
        return loc.getWorld().spawn(loc, WitherSkeleton.class, w -> {
            KeyUtils.set(w, key(Difficulty.EXCITING), true);
            w.setInvulnerable(true);
            KeyUtils.set(w, key, false);
            GlowUtils.glow(w, NamedTextColor.BLACK);
            w.getEquipment().setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
            w.setRemoveWhenFarAway(false);
            AttributeInstance speed = w.getAttribute(Attribute.MOVEMENT_SPEED);
            if (speed != null) speed.setBaseValue(0.4);
        });
    }
}
