package xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

public class ConductableItems {
    private final XmasLegacy plugin;
    private static ConductableItems instance;

    public static ConductableItems getInstance() {
        if (instance == null) {
            instance = new ConductableItems();
        }
        return instance;
    }

    private ConductableItems() {
        this.plugin = XmasLegacy.getInstance();
    }

    //값싼 기초물약
    public ItemStack DragonPotion() {
        return ItemBuilder.of(plugin, Material.DRAGON_BREATH)
                .setName(ColorUtils.chat("&d&l용의 숨결"))
                .setLore(ColorUtils.chat("&7가장 기초적인 물약. 약간의 버프를 잠시 받는다."), ColorUtils.chat("&8ELPMETRORRIM"))
                .setGlint(true)
                .setTag("potion", "dragon_breath")
                .setMaxStackSize(64)
                .build()
                .clone();
    }

    //힐러물약
    public ItemStack HealerPotion() {
        return ItemBuilder.of(plugin, Material.POTION)
                .setName(ColorUtils.chat("&e&l회복 포션"))
                .setLore(ColorUtils.chat("&7일정 시간동안 회복을 받습니다."))
                .customPotionColor(Color.ORANGE)
                .setTag("potion", "healing")
                .setMaxStackSize(64)
                .setGlint(false)
                .build()
                .clone();
    }

    //보호물약
    public ItemStack ProtectionPotion() {
        return ItemBuilder.of(plugin, Material.POTION)
                .setName(ColorUtils.chat("&b&l보호의 물약"))
                .setLore(ColorUtils.chat("&7&l잠깐동안 보호막이 생겨 피해를 감소받는다."))
                .customPotionColor(Color.NAVY)
                .setTag("potion", "protection")
                .setGlint(false)
                .setMaxStackSize(64)
                .build()
                .clone();
    }

    //스피어탄
    public ItemStack SpearPotion() {
        return ItemBuilder.of(plugin, Material.SPLASH_POTION)
                .setName(ColorUtils.chat("&e&l스피어탄"))
                .setLore(ColorUtils.chat("&7투척 위치에 창을 소환한다."))
                .customPotionColor(Color.YELLOW)
                .setTag("potion", "spear")
                .setMaxStackSize(64)
                .setGlint(true)
                .build()
                .clone();
    }

    //???
    public ItemStack DeathSave() {
        return ItemBuilder.of(plugin, Material.POTION)
                .setName(ColorUtils.chat("&4&l???"))
                .setLore(ColorUtils.chat("&7죽기 직전에 작용시 그 죽음을 취소한다."))
                .customPotionColor(Color.RED)
                .setGlint(true)
                .setTag("potion", "death_saver")
                .setMaxStackSize(64)
                .build()
                .clone();
    }
}
