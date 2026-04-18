package org.lazberry.xmasLegacy.Region;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.lazberry.xmasLegacy.Roles.Roles;
import org.lazberry.xmasLegacy.UserManager;

import java.util.UUID;

public class Region {
    private final UUID owner;
    private final String id;
    private final Location center;
    private final World world;
    private final UserManager UM;

    // 계산된 좌표들을 상수로 들고 있습니다 (성능 최적화)
    private final int sMinX, sMaxX, sMinZ, sMaxZ, sMinY, sMaxY;
    private final int oMinX, oMaxX, oMinZ, oMaxZ, oMinY, oMaxY;

    // 권한 설정 관련 변수 (추가된 부분)
    private boolean allowPublicEntry = true;      // 외부인 출입 허용 여부
    private boolean allowPublicInteraction = false; // 외부인 상호작용 허용 여부

    public Region(Player p, Location center, UserManager UM) {
        this.UM = UM;
        this.owner = p.getUniqueId();
        this.center = center;
        this.id = IDGenerator.generateRandomId();
        this.world = center.getWorld();

        Roles role = UM.getUser(p).getRole();

        int minY = (role == Roles.MINER) ? -100 : 15;
        this.sMinY = minY;
        this.oMinY = minY;
        this.sMaxY = 320;
        this.oMaxY = 320;

        this.sMinX = center.getBlockX() - 5;
        this.sMaxX = center.getBlockX() + 5;
        this.sMinZ = center.getBlockZ() - 5;
        this.sMaxZ = center.getBlockZ() + 5;

        this.oMinX = center.getBlockX() - 10;
        this.oMaxX = center.getBlockX() + 10;
        this.oMinZ = center.getBlockZ() - 10;
        this.oMaxZ = center.getBlockZ() + 10;
    }

    // 판정 로직: &&를 사용하여 박스 안에 있는지 정확히 체크
    public boolean isInsideSafeZone(Location loc) {
        if (!loc.getWorld().equals(world)) return false;
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return (x >= sMinX && x <= sMaxX) &&
                (y >= sMinY && y <= sMaxY) &&
                (z >= sMinZ && z <= sMaxZ);
    }

    public boolean isInsideOuterZone(Location loc) {
        if (!loc.getWorld().equals(world)) return false;
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return (x >= oMinX && x <= oMaxX) &&
                (y >= oMinY && y <= oMaxY) &&
                (z >= oMinZ && z <= oMaxZ);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region c)) return false;
        return owner == c.owner;
    }

    @Override
    public int hashCode() {
        return owner.hashCode();
    }
    public UUID getOwner() { return owner; }
    public String getId() { return id; }
    public Location getCenter() { return center; }
    public boolean isAllowPublicEntry() { return allowPublicEntry; }
    public boolean isAllowPublicInteraction() { return allowPublicInteraction; }

    // YAML 로딩 전용 생성자 (기존 로직을 건드리지 않고 필드값만 그대로 주입)
    public Region(UUID owner, String id, Location center, boolean allowEntry, boolean allowInteract, UserManager UM) {
        this.owner = owner;
        this.id = id;
        this.center = center;
        this.world = center.getWorld();
        this.allowPublicEntry = allowEntry;
        this.allowPublicInteraction = allowInteract;
        this.UM = UM;

        Roles role = UM.getUser(Bukkit.getOfflinePlayer(owner).getPlayer()).getRole();

        int minY = (role == Roles.MINER) ? -100 : 15;
        this.sMinY = minY; this.oMinY = minY;
        this.sMaxY = 320; this.oMaxY = 320;

        this.sMinX = center.getBlockX() - 5;
        this.sMaxX = center.getBlockX() + 5;
        this.sMinZ = center.getBlockZ() - 5;
        this.sMaxZ = center.getBlockZ() + 5;

        this.oMinX = center.getBlockX() - 10;
        this.oMaxX = center.getBlockX() + 10;
        this.oMinZ = center.getBlockZ() - 10;
        this.oMaxZ = center.getBlockZ() + 10;
    }
}
