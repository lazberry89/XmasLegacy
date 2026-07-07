package org.lazberry.xmaslegacy.TransferPortal;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.PluginUtils.ServerType;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Portal {
    @EqualsAndHashCode.Include
    private final @NotNull String key;
    private final @NotNull @Getter Location center;
    private final @NotNull @Getter ServerType destination;
    private final @Nullable @Getter ItemDisplay flame;

    public Portal(@NotNull String key, @NotNull Location center, @NotNull ServerType type) {
        this.key = key;
        this.center = center;
        this.destination = type;
        this.flame = makeFlame();
    }

    //TODO make model and apply to Oraxen.
    private @NotNull ItemDisplay makeFlame() {
        return null;
    }

    @CheckReturnValue
    public boolean isStepping(@NotNull Player p) {
        return isStepping(p.getLocation());
    }

    @CheckReturnValue
    public boolean isStepping(@NotNull Location loc) {
        if (!loc.getWorld().equals(this.center.getWorld())) return false;

        double dx = Math.abs(loc.getX() - this.center.getX());
        double dz = Math.abs(loc.getZ() - this.center.getZ());

        double dy = Math.abs(loc.getY() - this.center.getY());

        return dx <= 2.5 && dz <= 2.5 && dy <= 3.0;
    }

    public @NotNull String key() {
        return this.key;
    }

}
