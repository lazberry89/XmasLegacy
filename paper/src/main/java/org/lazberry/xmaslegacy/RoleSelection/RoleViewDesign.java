package org.lazberry.xmaslegacy.RoleSelection;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Task
@Registry.Include(type = ServerType.MAIN)
public class RoleViewDesign implements Tasks {
    private final @NotNull XmasLegacy plugin;
    private final @NotNull ItemStack[][][] allFrames = new ItemStack[3][9][3];

    private @Nullable ItemStack RED;
    private @Nullable ItemStack WHITE;

    private int currentFrameIndex = 0;
    private @Nullable BukkitTask task;

	@Inject
    public RoleViewDesign(@NotNull XmasLegacy plugin) {
		this.plugin = plugin;
    }

    public void init() {
        this.RED = createGuiItem(Material.RED_STAINED_GLASS_PANE);
        this.WHITE = createGuiItem(Material.WHITE_STAINED_GLASS_PANE);

        for (int f = 0; f < 3; f++) {
            allFrames[f] = setupFrame(f);
        }
        this.startTask(plugin);
    }

    private @NotNull ItemStack createGuiItem(@NotNull Material material) {
        return ItemBuilder.of(plugin, material)
                .setName(ColorUtils.chat(""))
                .setLore(ColorUtils.chat(""))
                .hideAllFlags()
                .build().clone();
    }

    public @NotNull ItemStack[][] setupFrame(int frame) {
        ItemStack[][] frameI = new ItemStack[9][3];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 2 && j == 1 || i == 6 && j == 1) continue;
                frameI[i][j] = WHITE;
            }
        }

        switch (frame) {
            case 0 -> {
                frameI[0][0] = RED; frameI[2][0] = RED; frameI[1][1] = RED;
                frameI[0][2] = RED; frameI[5][2] = RED; frameI[4][1] = RED;
                frameI[3][2] = RED; frameI[8][0] = RED; frameI[7][1] = RED;
                frameI[6][2] = RED; frameI[8][2] = RED;
            }
            case 1 -> {
                frameI[1][0] = RED; frameI[0][1] = RED; frameI[4][0] = RED;
                frameI[3][1] = RED; frameI[2][2] = RED; frameI[7][0] = RED;
                frameI[6][1] = RED; frameI[5][2] = RED; frameI[8][1] = RED;
                frameI[7][2] = RED;
            }
            case 2 -> {
                frameI[2][0] = RED; frameI[1][1] = RED; frameI[0][2] = RED;
                frameI[5][0] = RED; frameI[4][1] = RED; frameI[3][2] = RED;
                frameI[8][0] = RED; frameI[7][1] = RED; frameI[6][2] = RED;
            }
        }
        return frameI;
    }

    @Override
    public void startTask(@NotNull XmasLegacy plugin) {
        if (task != null) task.cancel();

        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            ItemStack[][] currentFrameData = allFrames[currentFrameIndex];

            for (Player p : Bukkit.getOnlinePlayers()) {
                InventoryView view = p.getOpenInventory();
                Inventory topInventory = view.getTopInventory();

                if (topInventory.getHolder() instanceof RoleSelectionInterface) {
                    for (int i = 0; i < 9; i++) {
                        for (int j = 0; j < 3; j++) {
                            ItemStack item = currentFrameData[i][j];
                            if (item == null) continue;
                            topInventory.setItem((j + 1) * 9 + i, item);
                        }
                    }
                }
            }
            currentFrameIndex = (currentFrameIndex + 1) % 3;
        }, 0L, 5L);
    }

    @Override
    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}