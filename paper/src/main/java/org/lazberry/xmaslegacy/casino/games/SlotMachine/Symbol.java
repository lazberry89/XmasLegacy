package org.lazberry.xmaslegacy.casino.games.SlotMachine;

import lombok.Getter;
import org.bukkit.Material;

@Getter
public enum Symbol {
    DIAMOND("&e&lDIAMOND", Material.DIAMOND_BLOCK),
    GOLD("&6&lGOLD", Material.GOLD_BLOCK),
    SILVER("&f&lSILVER", Material.IRON_BLOCK),
    NO_LUCK("&c&lNO LUCK!", Material.WAXED_OXIDIZED_COPPER);

    private final String title;
    private final Material showMaterial;

    Symbol(String title, Material showMaterial) {
        this.title = title;
        this.showMaterial = showMaterial;
    }
}
