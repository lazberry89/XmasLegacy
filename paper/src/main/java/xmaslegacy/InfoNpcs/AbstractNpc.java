package xmaslegacy.InfoNpcs;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.XmasLegacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractNpc {
    protected final @NotNull Map<UUID, Integer> playerCaption = new HashMap<>();
    protected final @NotNull List<String> caption;
    private final @NotNull @Getter XmasLegacy plugin;
    private final @NotNull @Getter NamespacedKey key;

    protected AbstractNpc(@NotNull List<String> cap) {
        this.plugin = XmasLegacy.getInstance();
        this.key = plugin.getNamespacedKey("npc");
        this.caption = cap;
    }

    protected abstract @NotNull String next(@NotNull Player player);
    public abstract void sendCaption(@NotNull Player player);
}
