package xmaslegacy.HuntingZone.CustomMobs;

import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;

public enum MobGrade {
    UNRATED(NamedTextColor.AQUA),
    NAMED(NamedTextColor.BLUE),
    HONORED(NamedTextColor.DARK_BLUE),
    ELITE(NamedTextColor.DARK_PURPLE),
    MYTHIC(NamedTextColor.RED),
    BOSS(NamedTextColor.DARK_RED);

    private final NamedTextColor color;

    MobGrade(NamedTextColor color) {
        this.color = color;
    }

    public @NotNull NamedTextColor color() {
        return this.color;
    }
}
