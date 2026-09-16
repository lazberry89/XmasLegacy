package org.lazberry.xmaslegacy.casino.games.CoinFlip;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CoinFlip {
    public static final NamespacedKey key = KeyUtils.get("CoinFlip");
    private final double chanceUserWin = 0.48;
    @EqualsAndHashCode.Include
    private final String id;
    private final Location location;
    private final ItemStack displayItem;
    private @Setter State result = State.FRONT;

    public enum State {
        FRONT,
        ROLLING,
        BACK;

        State reverse() {
            return switch (this) {
                case BACK -> FRONT;
                case FRONT -> BACK;
                case ROLLING -> ROLLING;
            };
        }
    }

    public CoinFlip(String id, Location location, ItemStack displayItem) {
        this.id = id;
        this.location = location;
        this.displayItem = displayItem;
    }

    public ItemDisplay spawnDisplay(Location loc) {
        return loc.getWorld().spawn(loc, ItemDisplay.class, i -> {
            i.setItemStack(displayItem);
            KeyUtils.set(i, key, "key");
            GlowUtils.glow(i, NamedTextColor.RED);
        });
    }
}
