package org.lazberry.xmaslegacy.mining;

import org.bukkit.Location;
import org.jetbrains.annotations.Contract;
import org.lazberry.xmaslegacy.utils.Axiom;

public record MineField(Location loc1, Location loc2) {

    @Contract(pure = true)
    public boolean isInside(Location loc) {
        return Axiom.isInBoundingBox(loc, loc1, loc2);
    }

    public double sizeHorizontal() {
        return Axiom.distanceHorizontal(loc1, loc2);
    }
}
