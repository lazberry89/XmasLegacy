package xmaslegacy.Gacha;

import org.jetbrains.annotations.NotNull;

public enum GachaGrade {
    NORMAL("&7&l", "일반"),
    RARE("&a&l", "희귀"),
    MYTHIC("&c&l", "신화"),
    LEGENDARY("&e&l", "전설"),
    ULTIMATE("", "<#A800FF>&lU<#B900F2>&lL<#CA00E5>&lT<#DB00D8>&lI<#EC00CB>&lM<#FD00BE>&lA<#FF00B1>&lT<#FF00A4>&lE");

    private final String color;
    private final String key;

    GachaGrade(String color, String key) {
        this.color = color;
        this.key = key;
    }

    public @NotNull String getColor() {
        return this.color;
    }
    public @NotNull String getKey() {
        return this.key;
    }
}
