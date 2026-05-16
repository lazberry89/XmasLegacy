package org.lazberry.xmaslegacy.settings;

import net.kyori.adventure.text.Component;
import org.lazberry.xmaslegacy.ColorUtils;

public enum Tier implements ServerPrefix {
    VISITOR("&8[ VISITOR ]"),
    USER("&7[ USER ]"),
    NEIGHBOR("&a[ NEIGHBOR ]"),
    CELEBRITY("&b[ CELEBRITY ]"),
    HERO("&d[ HERO ]"),
    MYTHIC("&c[ MYTHIC ]"),
    LEGENDARY("&e[ LEGENDARY ]"),
    OVERLORD("&#FFC522[ &#FAA11CO&#F88F19V&#F57D16E&#F36B13R&#F05A0FL&#EE480CO&#EB3609R&#E92406D &#E40000]"),
    ETERNAL("&#49D6F1[ &#51BCE5E&#55AFDFT&#59A2D9E&#5D95D4R&#6188CEN&#657BC8A&#696EC2L &#7154B6]");

    private final String prefix;

    Tier(String prefix) {
        this.prefix = prefix;
    }

    public Component prefix() {
        return ColorUtils.chat(this.prefix);
    }
}
