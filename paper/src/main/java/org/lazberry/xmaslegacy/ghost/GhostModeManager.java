package org.lazberry.xmaslegacy.ghost;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;

import java.util.*;

@Registry
public class GhostModeManager implements Initiator {
    private final @NotNull Set<UUID> isGhostMode = new HashSet<>();
    private final @NotNull Map<UUID, ItemStack[]> saveArmor = new HashMap<>();
    private final @NotNull XmasLegacy plugin;

	@Inject
	public GhostModeManager(@NotNull XmasLegacy plugin) {
		this.plugin = plugin;
	}

	@NotNull Set<UUID> getGhostModePlayer() {
		return new HashSet<>(isGhostMode);
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

    public void toggle(@NotNull Player p) {
        if (isGhostMode.contains(p.getUniqueId())) DeGhostMode(p);
        else ghostMode(p);
    }

    public boolean isGhostMode(@NotNull Player p) {
        return isGhostMode.contains(p.getUniqueId());
    }
    public boolean isGhostMode(@NotNull UUID uuid) {
        return isGhostMode.contains(uuid);
    }

	@Override
	public void init() {

	}
}
