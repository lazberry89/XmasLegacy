package xmaslegacy.RoleSelection;

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
import xmaslegacy.Utils.ItemBuilder;
import xmaslegacy.XmasLegacy;

public enum RoleViewDesign {
	INSTANCE;

    private final @NotNull XmasLegacy plugin;
    private final @NotNull ItemStack[][][] allFrames = new ItemStack[3][9][3];
    private final @NotNull ItemStack RED;
    private final @NotNull ItemStack WHITE;

    private int currentFrameIndex = 0;
    private @Nullable BukkitTask task;

	RoleViewDesign() {
        this.plugin = XmasLegacy.getInstance();

        this.RED = createGuiItem(Material.RED_STAINED_GLASS_PANE);
        this.WHITE = createGuiItem(Material.WHITE_STAINED_GLASS_PANE);

    }

    public void init() {
        for (int f = 0; f < 3; f++) {
            allFrames[f] = setupFrame(f);
        }
        for (int f = 0; f < 3; f++) {
            allFrames[f] = setupFrame(f);
        }
        startVisualLoop();
    }

    private ItemStack createGuiItem(Material material) {
        return ItemBuilder.of(plugin, material)
                .setName(ColorUtils.chat(""))
                .setLore(ColorUtils.chat(""))
                .hideAllFlags()
                .build().clone();
    }

    public ItemStack[][] setupFrame(int frame) {
        ItemStack[][] frameI = new ItemStack[9][3];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 2 && j == 1 || i == 6 && j == 1) continue;
                frameI[i][j] = WHITE;
            }
        }

        // 사용자님의 3단계 패턴 로직
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

    public void startVisualLoop() {
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

    public void stopVisualLoop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}