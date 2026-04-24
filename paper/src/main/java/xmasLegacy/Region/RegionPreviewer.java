package xmasLegacy.Region;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import org.lazberry.xmaslegacy.Constants;
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RegionPreviewer {
    private final XmasLegacy plugin;
    private final RegionManager RM;
    private final HashMap<UUID, List<BlockDisplay>> activePreviews = new HashMap<>();

    public RegionPreviewer(XmasLegacy plugin, RegionManager RM) {
        this.plugin = plugin;
        this.RM = RM;
        startTask();
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : plugin.getServer().getOnlinePlayers()) {
                    if (isHoldingGenerator(p)) {
                        updatePreview(p);
                    } else {
                        clearPreview(p);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private boolean isHoldingGenerator(Player p) {
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType() != Material.BEACON || !item.hasItemMeta()) return false;
        String tag = item.getItemMeta().getPersistentDataContainer()
                .get(plugin.getNamespacedKey("RegionBeacon"), PersistentDataType.STRING);
        return "user".equals(tag);
    }

    private void updatePreview(Player p) {
        clearPreview(p);
        List<BlockDisplay> displays = new ArrayList<>();
        Location loc = p.getLocation().getBlock().getLocation();

        displays.add(createDisplay(p, loc, Constants.INNER_RANGE, 305, Material.RED_STAINED_GLASS, "inner")); // 안쪽 (5+1+5)
        displays.add(createDisplay(p, loc, Constants.OUTER_RANGE, 305, Material.BLUE_STAINED_GLASS, "outer")); // 바깥 (10+1+10)

        for (Region region : RM.getRegions()) {
            if (region.getCenter().distance(loc) < Constants.OUTER_RANGE) {
                displays.add(createDisplay(p, region.getCenter(), Constants.OUTER_RANGE, 305, Material.ORANGE_STAINED_GLASS, "existing"));
            }
        }

        activePreviews.put(p.getUniqueId(), displays);
    }

    private BlockDisplay createDisplay(Player p, Location center, int size, int height, Material mat, String type) {
        Location spawnLoc = center.clone().add(-(size / 2.0), 15, -(size / 2.0));

        BlockDisplay display = (BlockDisplay) center.getWorld().spawnEntity(spawnLoc, EntityType.BLOCK_DISPLAY);
        display.setBlock(mat.createBlockData());

        Transformation trans = display.getTransformation();
        trans.getScale().set(new Vector3f(size, height, size));
        display.setTransformation(trans);

        for (Player online : plugin.getServer().getOnlinePlayers()) {
            if (!online.equals(p)) online.hideEntity(plugin, display);
        }

        return display;
    }

    public void clearPreview(Player p) {
        if (activePreviews.containsKey(p.getUniqueId())) {
            activePreviews.get(p.getUniqueId()).forEach(BlockDisplay::remove);
            activePreviews.remove(p.getUniqueId());
        }
    }
}
