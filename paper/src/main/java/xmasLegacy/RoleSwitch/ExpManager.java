package xmasLegacy.RoleSwitch;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import xmasLegacy.XmasLegacy;

public class ExpManager {
    private final XmasLegacy plugin;
    private final UserManager um;

    public ExpManager(XmasLegacy plugin) {
        this.plugin = plugin;
        this.um = plugin.UM;
    }
    /*
    @Contract(pure = true)
    public boolean promote(@NotNull Player p, @NotNull Role currentRole, @NotNull Role target) {
        User user = um.getUser(p.getUniqueId());
        if (currentRole.getTier() >= target.getTier()) return false;
        if (user == null) return false;

        if (currentRole.next().contains(target)) {
            user.setRole(target);
            return true;
        }
        return false;
    }
    */

    public boolean addExp(@NotNull Player p, double amount) {
        User user = um.getUser(p.getUniqueId());
        if (user == null) return false;
        user.addExp(amount);
        user.setLevel((int) (user.getExp() / 10));
        return true;
    }

    @Contract(pure = true)
    public double calculateBuff(double origin, double percent) {
        return origin + (origin * percent);
    }
}
