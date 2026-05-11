package xmasLegacy.RoleSelection;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;

public class RoleViewDesign {
    private final XmasLegacy plugin;
    private final Map<Integer, ItemStack[][]> ItemMap = new HashMap<>();
    private final ItemStack RED = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    private final ItemStack WHITE = new ItemStack(Material.WHITE_STAINED_GLASS_PANE);

    public RoleViewDesign(XmasLegacy plugin) {
        this.plugin = plugin;
    }

    public ItemStack[][] setupFrame(int frame) {
        ItemStack[][] frameI = new ItemStack[9][3];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 2 && j == 1 || i == 6 && j == 1) continue;
                frameI[i][j] = WHITE;
            }
        }
        switch (frame) {
            case 0 -> {
                frameI[0][0] = RED;
                frameI[2][0] = RED;
                frameI[1][1] = RED;
                frameI[0][2] = RED;
                frameI[5][2] = RED;
                frameI[4][1] = RED;
                frameI[3][2] = RED;
                frameI[8][0] = RED;
                frameI[7][1] = RED;
                frameI[6][2] = RED;
                frameI[8][2] = RED;
            }
            case 1 -> {}
        }
        return frameI;
    }
}
