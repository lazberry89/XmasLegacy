package xmasLegacy.TransferPortal;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmasLegacy.ServerType;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PortalManager {
    private final @NotNull Map<String, Portal> portalMap = new HashMap<>();
    private final @NotNull Set<Portal> portalSet = new HashSet<>();
    private final @NotNull XmasLegacy plugin;
    private final @NotNull NamespacedKey key;

    private static volatile PortalManager instance;

    public static PortalManager getInstance() {
        if (instance == null) {
            synchronized (PortalManager.class) {
                if (instance == null) instance = new PortalManager();
            }
        }
        return instance;
    }

    private PortalManager() {
        this.plugin = XmasLegacy.getInstance();
        this.key = plugin.getNamespacedKey("portal");
    }

    public void addPortal(@NotNull String key, @NotNull Location loc, @NotNull ServerType destination) {
        Portal portal = new Portal(key, loc, destination);
        this.portalMap.put(key, portal);
        this.portalSet.add(portal);
    }

    public boolean removePortal(@NotNull String key) {
        Portal portal = this.portalMap.remove(key);
        if (portal == null) return false;

        this.portalSet.remove(portal);
        return true;
    }

    public @Nullable Portal getPortal(@NotNull Location loc) {
        return this.portalSet.stream()
                .filter(p -> p.isStepping(loc))
                .findFirst().orElse(null);
    }

    public @Nullable Portal getPortal(@NotNull String key) {
        return this.portalMap.get(key);
    }

    public @Nullable Portal getPortal(@NotNull Player player) {
        return getPortal(player.getLocation());
    }
}
