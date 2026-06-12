package xmasLegacy.TransferPortal;

import com.google.common.base.Objects;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmasLegacy.ServerType;

public class Portal {
    private final @NotNull String key;
    private final @NotNull Location center;
    private final @NotNull ServerType destination;
    private final @Nullable ItemDisplay flame;

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
    public @Nullable ItemDisplay getFlame() {
        return this.flame;
    }
    public @NotNull String key() {
        return this.key;
    }
    public @NotNull Location getCenter() {
        return this.center;
    }
    public @NotNull ServerType getDestination() {
        return this.destination;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Portal p)) return false;
        return Objects.equal(p.key(), this.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.key);
    }

}
