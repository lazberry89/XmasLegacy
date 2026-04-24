package xmasLegacy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GhostModeManager {
    private final Map<UUID, Boolean> isGhostMode = new HashMap<>();
    private final Map<UUID, ItemStack[]> saveArmor = new HashMap<>();
    private final XmasLegacy plugin;

    public GhostModeManager(XmasLegacy plugin) {
        this.plugin = plugin;
    }

    public void ghostMode(Player p) {
        if (!p.isOp()) return;
        if (Boolean.TRUE.equals(isGhostMode.get(p.getUniqueId()))) return;
        ItemStack[] saveArmor = p.getInventory().getArmorContents();
        this.saveArmor.put(p.getUniqueId(),  saveArmor);
        p.getInventory().setArmorContents(new ItemStack[4]);

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.hidePlayer(plugin, p);
        }

        p.setInvisible(true);
        p.setCollidable(false);
        isGhostMode.put(p.getUniqueId(), Boolean.TRUE);
    }

    public void DeGhostMode(Player p) {
        if (Boolean.FALSE.equals(isGhostMode.get(p.getUniqueId()))) return;
        p.getInventory().setArmorContents(saveArmor.get(p.getUniqueId()));
        p.setInvisible(false);
        p.setCollidable(true);
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, p);
        }
        saveArmor.remove(p.getUniqueId());
        isGhostMode.put(p.getUniqueId(), Boolean.FALSE);
    }

    public void toggle(Player p) {
        if (isGhostMode.getOrDefault(p.getUniqueId(), false)) {
            DeGhostMode(p);
        } else {
            ghostMode(p);
        }
    }

    public boolean  isGhostMode(Player p) {
        return isGhostMode.getOrDefault(p.getUniqueId(), false);
    }

    public Map<UUID, Boolean> isGhostMode() {
        return isGhostMode;
    }
}
