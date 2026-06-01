package xmasLegacy.HuntingZone.CustomMobs.Level1;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.TriState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.HuntingZone.CustomMobs.MobGrade;
import xmasLegacy.HuntingZone.CustomMobs.MobKey;
import xmasLegacy.Utils.GlowUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

public class IcedZombie implements CustomMob, UnratedMob {
    private final MobGrade grade;
    private final MobKey key;
    private final ItemStack chestplate;
    private final ItemStack weapon;
    private final XmasLegacy plugin;
    private static IcedZombie instance;

    public static IcedZombie getInstance() {
        if (instance == null) instance = new IcedZombie();
        return instance;
    }

    private IcedZombie() {
        this.grade = MobGrade.UNRATED;
        this.key = MobKey.ICED_ZOMBIE;
        this.chestplate = makeTool(EquipmentSlot.CHEST);
        this.weapon = makeTool(EquipmentSlot.HAND);
        this.plugin = XmasLegacy.getInstance();
    }

    private ItemStack makeTool(EquipmentSlot slot) {
        if (slot.equals(EquipmentSlot.HAND)) return ItemBuilder.of(plugin, Material.STONE_SWORD)
                .setName(ColorUtils.chat("&b얼어붙은 돌검"))
                .setUnbreakable()
                .build().clone();
        else return ItemBuilder.of(plugin, Material.IRON_CHESTPLATE)
                .setGlint(true)
                .setUnbreakable()
                .build().clone();
    }

    @Override
    public @NotNull MobGrade getGrade() {
        return this.grade;
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
        });
    }

    @Override
    public void attack(LivingEntity target) {} //lvl 1몹은 일반 바닐라 몹 평타
}
