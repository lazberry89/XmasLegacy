package org.lazberry.xmaslegacy.casino.games.CoinFlip;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CoinFlip {
    private final double chanceUserWin = 0.48;
    @EqualsAndHashCode.Include
    private final String id;
    private final Location location;
    private final ItemStack displayItem;
    private @Setter State coinState;

    public enum State {
        FRONT, BACK, ROLLING
    }

    public CoinFlip(String id, Location location, ItemStack displayItem) {
        this.id = id;
        this.location = location;
        this.displayItem = displayItem;
    }

}
