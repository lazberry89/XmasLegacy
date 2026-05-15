package org.lazberry.xmaslegacy.settings;

import net.kyori.adventure.text.Component;
import org.lazberry.xmaslegacy.ColorUtils;

public enum Tier implements ServerPrefix {
    VISITOR(ColorUtils.chat("&8&l[ VISITOR ]")),
    USER(ColorUtils.chat("&7&l[ USER ]")),
    NEIGHBOR(ColorUtils.chat("&a&l[ NEIGHBOR ]")),
    CELEBRITY(ColorUtils.chat("&b&l[ CELEBRITY ]")),
    HERO(ColorUtils.chat("&d&l[ HERO ]")),
    MYTHIC(ColorUtils.chat("&c&l[ MYTHIC ]")),
    LEGENDARY(ColorUtils.chat("&e&l[ LEGENDARY ]")),
    OVERLORD(ColorUtils.chat("&#FFC522[ &#FAA11CO&#F88F19V&#F57D16E&#F36B13R&#F05A0FL&#EE480CO&#EB3609R&#E92406D &#E40000]")),
    ETERNAL(ColorUtils.chat("&#49D6F1[ &#51BCE5E&#55AFDFT&#59A2D9E&#5D95D4R&#6188CEN&#657BC8A&#696EC2L &#7154B6]"));

    private final Component prefix;

    Tier(Component prefix) {
        this.prefix = prefix;
    }

    public Component prefix() {
        return this.prefix;
    }
}
