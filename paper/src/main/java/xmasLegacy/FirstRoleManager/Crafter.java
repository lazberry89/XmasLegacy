package xmasLegacy.FirstRoleManager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Roles.Roles;
import xmasLegacy.XmasLegacy;

public class Crafter extends AbstractFirstRole{

    public Crafter(int c1, int c2, XmasLegacy plugin) {
        super(c1, c2, plugin);
    }

    @Override
    public void useFirstSkill(Player p) {

    }

    @Override
    public void useSecondSkill(Player p) {

    }

    @Override
    public @NotNull Roles getRole() {
        return null;
    }

    @Override
    public @NotNull ItemStack roleWeapon() {
        return null;
    }

    @Override
    public @NotNull ItemStack roleArmor() {
        return null;
    }

    @Override
    public @NotNull ItemStack roleBook() {
        return null;
    }
}
