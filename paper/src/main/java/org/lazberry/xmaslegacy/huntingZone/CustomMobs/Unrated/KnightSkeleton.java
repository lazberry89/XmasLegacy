package org.lazberry.xmaslegacy.huntingZone.CustomMobs.Unrated;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.huntingZone.CustomMobs.CustomMob;
import org.lazberry.xmaslegacy.huntingZone.CustomMobs.MobKey;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

import java.util.concurrent.ThreadLocalRandom;

public class KnightSkeleton extends AbstractUnratedMobs implements CustomMob, UnratedMob {

    @Override
    public @NotNull MobKey getKey() {
        return MobKey.KNIGHT_SKELETON;
    }

    @Override
    public @NotNull LivingEntity spawn(@NotNull Location loc) {
        return loc.getWorld().spawn(loc, Skeleton.class, s -> {
            GlowUtils.glow(s, getGrade().color());
            s.getEquipment().setHelmet(null);
            s.getEquipment().setLeggings(null);
            s.getEquipment().setBoots(null);
            s.getEquipment().setItemInMainHand(null);
            s.clearActiveItem();
            s.setArrowsInBody(ThreadLocalRandom.current().nextInt(1, 11));
            s.getEquipment().setDropChance(EquipmentSlot.CHEST, 0.0f);
            s.getEquipment().setDropChance(EquipmentSlot.FEET, 0.0f);
            s.getEquipment().setChestplate(chestplate());
            s.getEquipment().setBoots(boots());
            s.getEquipment().setItemInMainHand(weapon());
        });
    }

    private ItemStack chestplate() {
        return ItemBuilder.of(plugin, Material.IRON_CHESTPLATE)
                .setGlint(true)
                .build();
    }

    private ItemStack boots() {
        return ItemBuilder.of(plugin, Material.IRON_BOOTS)
                .setGlint(true)
                .build();
    }

    private ItemStack weapon() {
        return ItemBuilder.of(plugin, Material.IRON_SPEAR)
                .addEnchant(Enchantment.KNOCKBACK, 5)
                .build();
    }

    @Override
    public void attack(LivingEntity target) {

    }
}
