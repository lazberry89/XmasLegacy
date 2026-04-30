package xmasLegacy.Region;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.IDGenerator;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.User.UserManager;

import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode")
public class Region {
    private final UUID owner;
    private final String id;
    private final Location center;
    private final World world;
    private final UserManager UM;
	private String name;

    private final int sMinX, sMaxX, sMinZ, sMaxZ, sMinY, sMaxY;
    private final int oMinX, oMaxX, oMinZ, oMaxZ, oMinY, oMaxY;

    private boolean allowPublicEntry = true;      // 외부인 출입 허용 여부
    private boolean allowPublicInteraction = false; // 외부인 상호작용 허용 여부

    public Region(Player p, Location center, UserManager UM) {
        this.UM = UM;
        this.owner = p.getUniqueId();
        this.center = center;
        this.id = IDGenerator.generateRandomId(Constants.ID_LENGTH);
        this.world = center.getWorld();
		this.name = p.getName();

        Roles role = UM.getUser(p.getUniqueId()).getRole();

        int minY = (Roles.MINER.equals(role)) ? Constants.MINER_MINY : Constants.USER_MINY;
        this.sMinY = minY;
        this.oMinY = minY;
        this.sMaxY = Constants.MAX_HEIGHT;
        this.oMaxY = Constants.MAX_HEIGHT;

        this.sMinX = center.getBlockX() - Constants.INNER_RANGE;
        this.sMaxX = center.getBlockX() + Constants.INNER_RANGE;
        this.sMinZ = center.getBlockZ() - Constants.INNER_RANGE;
        this.sMaxZ = center.getBlockZ() + Constants.INNER_RANGE;

        this.oMinX = center.getBlockX() - Constants.OUTER_RANGE;
        this.oMaxX = center.getBlockX() + Constants.OUTER_RANGE;
        this.oMinZ = center.getBlockZ() - Constants.OUTER_RANGE;
        this.oMaxZ = center.getBlockZ() + Constants.OUTER_RANGE;
    }

    public boolean isInsideSafeZone(@NotNull Location loc) {
        if (WorldDiff(loc)) return false;
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return (x >= sMinX && x <= sMaxX) &&
                (y >= sMinY && y <= sMaxY) &&
                (z >= sMinZ && z <= sMaxZ);
    }

    public boolean isInsideOuterZone(Location loc) {
        if (WorldDiff(loc)) return false;
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return (x >= oMinX && x <= oMaxX) &&
                (y >= oMinY && y <= oMaxY) &&
                (z >= oMinZ && z <= oMaxZ);
    }

    private boolean WorldDiff(Location loc) {
        return !loc.getWorld().equals(world);
    }

	public boolean overlaps(Region other) {
		if (!this.world.equals(other.world)) return false;

		boolean overlapX = this.oMinX <= other.oMaxX && this.oMaxX >= other.oMinX;
		boolean overlapY = this.oMinY <= other.oMaxY && this.oMaxY >= other.oMinY;
		boolean overlapZ = this.oMinZ <= other.oMaxZ && this.oMaxZ >= other.oMinZ;

		return overlapX && overlapY && overlapZ;
	}
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region c)) return false;
        return Objects.equals(id, c.getId());
    }

    @Override
    public int hashCode() {
        return owner.hashCode();
    }
    public UUID getOwner() { return owner; }
    public @NotNull String getId() { return id; }
    public @NotNull Location getCenter() { return center; }
    public boolean isAllowPublicEntry() { return allowPublicEntry; }
    public boolean isAllowPublicInteraction() { return allowPublicInteraction; }
	public void setAllowPublicEntry(boolean allowPublicEntry) { this.allowPublicEntry = allowPublicEntry; }
	public void setAllowPublicInteraction(boolean allowPublicInteraction) { this.allowPublicInteraction = allowPublicInteraction; }
	public @NotNull String getName() { return name; }
	public void setName(String name) { this.name = name; }


    public Region(UUID owner, String id, Location center, boolean allowEntry, boolean allowInteract, UserManager UM) {
        this.owner = owner;
        this.id = id;
        this.center = center;
        this.world = center.getWorld();
        this.allowPublicEntry = allowEntry;
        this.allowPublicInteraction = allowInteract;
        this.UM = UM;

        Roles role = UM.getRoleByUUID(owner);

	    int minY = (role == Roles.MINER) ? Constants.MINER_MINY : Constants.USER_MINY;
        this.sMinY = minY; this.oMinY = minY;
        this.sMaxY = Constants.MAX_HEIGHT; this.oMaxY = Constants.MAX_HEIGHT;

        this.sMinX = center.getBlockX() - Constants.INNER_RANGE;
        this.sMaxX = center.getBlockX() + Constants.INNER_RANGE;
        this.sMinZ = center.getBlockZ() - Constants.INNER_RANGE;
        this.sMaxZ = center.getBlockZ() + Constants.INNER_RANGE;

        this.oMinX = center.getBlockX() - Constants.OUTER_RANGE;
        this.oMaxX = center.getBlockX() + Constants.OUTER_RANGE;
        this.oMinZ = center.getBlockZ() - Constants.OUTER_RANGE;
        this.oMaxZ = center.getBlockZ() + Constants.OUTER_RANGE;
    }
}
