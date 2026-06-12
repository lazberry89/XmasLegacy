package xmasLegacy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public enum GhostModeManager {
	INSTANCE;

    private final @NotNull Set<UUID> isGhostMode = new HashSet<>();
    private final @NotNull Map<UUID, ItemStack[]> saveArmor = new HashMap<>();
    private final @NotNull XmasLegacy plugin;

	GhostModeManager() {
        this.plugin = XmasLegacy.getInstance();
    }

    public void ghostMode(Player p) {
        if (!p.isOp()) return;
        if (isGhostMode.contains(p.getUniqueId())) return;
		ItemStack[] saveArmor = p.getInventory().getArmorContents();
        this.saveArmor.put(p.getUniqueId(), saveArmor);
        p.getInventory().setArmorContents(new ItemStack[4]);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.hidePlayer(plugin, p);
        }

        p.setInvisible(true);
        p.setCollidable(false);
        isGhostMode.add(p.getUniqueId());
    }

    public void DeGhostMode(Player p) {
        if (!isGhostMode.contains(p.getUniqueId())) return;
        p.getInventory().setArmorContents(saveArmor.get(p.getUniqueId()));
        p.setInvisible(false);
        p.setCollidable(true);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, p);
        }
        saveArmor.remove(p.getUniqueId());
        isGhostMode.add(p.getUniqueId());
    }

    public void toggle(Player p) {
        if (isGhostMode.contains(p.getUniqueId())) {
            DeGhostMode(p);
        } else {
            ghostMode(p);
        }
    }

    public boolean isGhostMode(Player p) {
        return isGhostMode.contains(p.getUniqueId());
    }
    public boolean isGhostMode(UUID uuid) {
        return isGhostMode.contains(uuid);
    }

    public Set<UUID> isGhostMode() {
        return isGhostMode;
    }
}
