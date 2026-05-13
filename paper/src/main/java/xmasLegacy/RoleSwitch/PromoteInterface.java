package xmasLegacy.RoleSwitch;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
public class PromoteInterface implements InventoryHolder {
    private final Inventory inv;
    private final UserManager um;
    private final Map<Integer, Role> roleMap = new HashMap<>();

    public PromoteInterface(XmasLegacy plugin, Player view) {
        this.um = plugin.UM;
        User user = um.getUser(view.getUniqueId());

        this.inv = Bukkit.createInventory(this, 9, ColorUtils.chat("&b&l전직 루트 선택"));

        ItemStack bg = ItemBuilder.of(plugin, Material.GRAY_STAINED_GLASS_PANE).setName(ColorUtils.chat("")).hideAllFlags().build();
        for (int i = 0; i < 9; i++) inv.setItem(i, bg);

        if (user == null) return;

        List<Role> roles = user.getRole().next();
        int[] slots = {1, 4, 7};

        for (int i = 0; i < slots.length; i++) {
            if (i < roles.size() && roles.get(i) != null) {
                Role targetRole = roles.get(i);

                ItemStack item = ItemBuilder.of(plugin, Material.WRITTEN_BOOK)
                        .setName(ColorUtils.chat("&6&l" + targetRole.getKor()))
                        .setLore(ColorUtils.chat("&7클릭 시 전직으로 이동합니다 >"))
                        .hideAllFlags()
                        .build();

                inv.setItem(slots[i], item);
                this.roleMap.put(slots[i], roles.get(i));
            } else {
                ItemStack barrier = ItemBuilder.of(plugin, Material.BARRIER)
                        .setName(ColorUtils.chat("&c&l준비중..."))
                        .setLore(ColorUtils.chat("&7이용해주셔서 감사합니다!"))
                        .build();

                inv.setItem(slots[i], barrier);
                this.roleMap.put(slots[i], Roles.USER);
            }
        }
    }

    public @Nullable Role getRoleBySlot(int slot) {
        return this.roleMap.get(slot);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
}
