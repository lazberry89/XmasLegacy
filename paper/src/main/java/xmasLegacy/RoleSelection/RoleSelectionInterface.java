package xmasLegacy.RoleSelection;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.FirstRoleManager.FirstRoleManager;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

@SuppressWarnings("FieldCanBeLocal")
public class RoleSelectionInterface implements InventoryHolder {
    private final Inventory inv;
    private final XmasLegacy plugin;
    private final FirstRoleManager rlm;
    private final Roles selectedRole;
    private final Component title;
    private final AbstractFirstRole selInst;
    /*
    00 01 02 03 04 05 06 07 08
    09 10 11 12 13 14 15 16 17
    18 19 20 21 22 23 24 25 26
    */

    public RoleSelectionInterface(Roles roles) {
        this.plugin = JavaPlugin.getPlugin(XmasLegacy.class);
        this.rlm = FirstRoleManager.INSTANCE;
        this.selectedRole = roles;
        this.title = ColorUtils.chat(String.format("&c&l[ %s ]", roles.getKor()));
        this.inv = Bukkit.createInventory(this, 36, title);
        ItemStack bg = ItemBuilder.of(plugin, Material.GRAY_STAINED_GLASS_PANE)
                .setName(ColorUtils.chat("")).setLore(ColorUtils.chat("")).hideAllFlags().build().clone();
        for (int i = 0; i < 9; i++) this.inv.setItem(i, bg);
        this.selInst = rlm.getRoleInstance(selectedRole);
        ItemStack tool1 = selInst.roleWeapon();
        ItemStack tool2 = selInst.roleArmor();
        ItemStack tool3 = selInst.roleBook();
        this.inv.setItem(1, tool1);
        this.inv.setItem(4, tool2);
        this.inv.setItem(7, tool3);
        ItemStack select = ItemBuilder.of(plugin, Material.BLUE_WOOL)
                .setName(ColorUtils.chat(String.format("&9%s 선택", this.selectedRole)))
                .setLore(ColorUtils.chat("&7한번 선택한 직업은 다시는&4 바꿀 수 없습니다&7."))
                .hideAllFlags()
                .build().clone();
        ItemStack goback = ItemBuilder.of(plugin, Material.RED_WOOL)
                .setName(ColorUtils.chat("&c&l돌아가기"))
                .setLore(ColorUtils.chat("&7직업 선택창으로 다시 돌아갑니다."))
                .hideAllFlags()
                .build().clone();
        this.inv.setItem(20, select);
        this.inv.setItem(24, goback);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inv;
    }
    public @NotNull Roles getRole() {
        return this.selectedRole;
    }
    public @NotNull Component Title() {
        return this.title;
    }
}
