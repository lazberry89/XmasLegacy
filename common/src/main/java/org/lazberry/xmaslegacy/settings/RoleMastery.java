package org.lazberry.xmaslegacy.settings;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;

public enum RoleMastery implements ServerPrefix {
    BEGINNER("&7⫝ BEGINNER"),
    BRONZE("&6⫚ BRONZE"),
    SILVER("&7⨈ SILVER"),
    GOLD("&6⩞ GOLD"),
    CRYSTAL("&3⩝ CRYSTAL"),
    EMERALD("&a⫔ EMERALD"),
    INSANE("&d⩐ INSANE"),
    IMMORTAL("&4⪔ IMMORTAL"),
    INFINITE("&#C822FF⨝ &#9C36F9I&#8540F6N&#6F4AF3F&#5954F0I&#435EEDN&#2C68EAI&#1672E7T&#007CE4E");

    private final @NotNull String prefix;

    RoleMastery(@NotNull String prefix) {
        this.prefix = prefix;
    }

    public @NotNull Component prefix() {
        return ColorUtils.chat(this.prefix);
    }
}
