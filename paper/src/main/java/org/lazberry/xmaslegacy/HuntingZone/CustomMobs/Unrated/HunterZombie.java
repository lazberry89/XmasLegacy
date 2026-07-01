package org.lazberry.xmaslegacy.HuntingZone.CustomMobs.Unrated;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Husk;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.HuntingZone.CustomMobs.MobKey;
import org.lazberry.xmaslegacy.Utils.GlowUtils;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.KeyUtils;

@SuppressWarnings("unused")
public class HunterZombie extends AbstractUnratedMobs implements CustomMob, UnratedMob {
    private final @NotNull MobKey key;
    private final @NotNull ItemStack weapon;
    private final @NotNull ItemStack armor;

    public HunterZombie() {
        super();
        this.key = MobKey.HUNTER_ZOMBIE;
        this.weapon = weaponMaker();
        this.armor = ItemBuilder.of(plugin, Material.IRON_CHESTPLATE).setUnbreakable().build().clone();
    }

    private ItemStack weaponMaker() {
        var oraxen = OraxenItems.getItemById("ancient_axe");
        return ItemBuilder.of(plugin, oraxen == null ? new ItemStack(Material.STONE_AXE) : oraxen.build())
                .setUnbreakable()
                .build().clone();
    }

    @Override
    public @NotNull MobKey getKey() {
        return this.key;
    }

    @Override
    public @NotNull LivingEntity spawn(@NotNull Location loc) {
        return loc.getWorld().spawn(loc, Husk.class, h -> {
            GlowUtils.setGlowColor(h, getGrade().color());
            h.getEquipment().setHelmet(null);
            h.getEquipment().setLeggings(null);
            h.getEquipment().setBoots(null);
            h.getEquipment().setItemInMainHand(null);

            h.getEquipment().setChestplate(armor);
            h.getEquipment().setItemInMainHand(weapon);
            h.setAdult();

            h.getPersistentDataContainer().set(KeyUtils.get("custom_mobs"), PersistentDataType.STRING, key.name());
            h.getEquipment().setDropChance(EquipmentSlot.CHEST, 0);
            h.getEquipment().setDropChance(EquipmentSlot.HAND, 0);
        });
    }

    @Override
    public void attack(LivingEntity target) {}
}
