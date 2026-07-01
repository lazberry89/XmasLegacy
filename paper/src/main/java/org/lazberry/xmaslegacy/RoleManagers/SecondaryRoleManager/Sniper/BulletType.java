package org.lazberry.xmaslegacy.RoleManagers.SecondaryRoleManager.Sniper;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;

public enum BulletType {
    NORMAL(70, 14),
    SNEAKY(55, 8.5),
    STUN(20, 5),
    MAGICAL(300, 24);

    private final double distance;
    private final double damage;

    BulletType(double distance, double damage) {
        this.distance = distance;
        this.damage = damage;
    }

    public double getDistance() {
        return this.distance;
    }
    public double getDamage() {
        return this.damage;
    }

    @Range(from = 0, to = 3)
    @Contract(pure = true)
    public static BulletType getType(int num) {
        return switch (num) {
            case 0 -> NORMAL;
            case 1 -> SNEAKY;
            case 2 -> STUN;
            case 3 -> MAGICAL;
            default -> throw new IllegalStateException("Unexpected value: " + num);
        };
    }
}
