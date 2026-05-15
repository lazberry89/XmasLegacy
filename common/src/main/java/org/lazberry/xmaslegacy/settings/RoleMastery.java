package org.lazberry.xmaslegacy.settings;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;

public enum RoleMastery implements ServerPrefix {
    BEGINNER(ColorUtils.chat("&7&l⫝ BEGINNER")),
    BRONZE(ColorUtils.chat("&6&l⫚ BRONZE")),
    SILVER(ColorUtils.chat("&7&l⨈ SILVER")),
    GOLD(ColorUtils.chat("&6&l⩞ GOLD")),
    CRYSTAL(ColorUtils.chat("&3&l⩝ CRYSTAL")),
    EMERALD(ColorUtils.chat("&a&l⫔ EMERALD")),
    INSANE(ColorUtils.chat("&d&l⩐ INSANE")),
    IMMORTAL(ColorUtils.chat("&4&l⪔ IMMORTAL")),
    INFINITE(ColorUtils.chat("&#C822FF⨝ &#9C36F9I&#8540F6N&#6F4AF3F&#5954F0I&#435EEDN&#2C68EAI&#1672E7T&#007CE4E"));

    private final Component prefix;

    RoleMastery(Component prefix) {
        this.prefix = prefix;
    }

    public @NotNull Component prefix() {
        return this.prefix;
    }
}
