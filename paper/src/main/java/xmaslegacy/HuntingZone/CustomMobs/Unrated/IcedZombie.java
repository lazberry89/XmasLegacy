package xmaslegacy.HuntingZone.CustomMobs.Unrated;

import io.th0rgal.oraxen.api.OraxenItems;
import net.kyori.adventure.util.TriState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmaslegacy.HuntingZone.CustomMobs.MobKey;
import xmaslegacy.Utils.GlowUtils;
import xmaslegacy.Utils.ItemBuilder;
import xmaslegacy.Utils.KeyUtils;

public class IcedZombie extends AbstractUnratedMobs implements CustomMob, UnratedMob {
    private final @NotNull MobKey key;
    private final @NotNull ItemStack chestplate;
    private final @NotNull ItemStack weapon;

    public IcedZombie() {
        super();
        this.key = MobKey.ICED_ZOMBIE;
        this.chestplate = makeTool(EquipmentSlot.CHEST);
        this.weapon = makeTool(EquipmentSlot.HAND);
    }

    private ItemStack makeTool(EquipmentSlot slot) {
		var oraxen = OraxenItems.getItemById("ancient_sword");

        if (slot.equals(EquipmentSlot.HAND)) return ItemBuilder.of(plugin, oraxen == null ? new ItemStack(Material.STONE_SWORD) : oraxen.build())
                .setName(ColorUtils.chat("&b얼어붙은 돌검"))
                .setUnbreakable()
                .build().clone();
        else return ItemBuilder.of(plugin, Material.IRON_CHESTPLATE)
                .setGlint(true)
                .setUnbreakable()
                .build().clone();
    }

    @Override
    public @NotNull MobKey getKey() {
        return this.key;
    }

    @Override
    public @NotNull LivingEntity spawn(@NotNull Location loc) {
        return loc.getWorld().spawn(loc, Zombie.class, z -> {
            GlowUtils.setGlowColor(z, getGrade().color());
            z.customName(ColorUtils.chat("&b&l얼음 좀비"));
            z.setCustomNameVisible(true);
            z.setDespawnInPeacefulOverride(TriState.FALSE);

            z.getEquipment().setDropChance(EquipmentSlot.HAND, 0);
            z.getEquipment().setDropChance(EquipmentSlot.OFF_HAND, 0);
            z.getEquipment().setDropChance(EquipmentSlot.HEAD, 0);
            z.getEquipment().setDropChance(EquipmentSlot.CHEST, 0);
            z.getEquipment().setDropChance(EquipmentSlot.LEGS, 0);
            z.getEquipment().setDropChance(EquipmentSlot.FEET, 0);

            z.getEquipment().setChestplate(chestplate);
            z.getEquipment().setItemInMainHand(weapon);
            z.setAdult();

            z.getPersistentDataContainer().set(KeyUtils.get("custom_mobs"), PersistentDataType.STRING, getKey().name());
        });
    }

    @Override
    public void attack(LivingEntity target) {} //lvl 1몹은 일반 바닐라 몹 평타
}
