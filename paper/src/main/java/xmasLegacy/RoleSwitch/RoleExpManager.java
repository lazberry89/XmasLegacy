package xmasLegacy.RoleSwitch;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Prefix;
import xmasLegacy.XmasLegacy;

public class RoleExpManager {
    private final XmasLegacy plugin;
    private final UserManager um;

    public RoleExpManager(XmasLegacy plugin) {
        this.plugin = plugin;
        this.um = plugin.UM;
    }

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

    @Contract(pure = true)
    public void addExp(@NotNull Player p, double amount) {
        User user = um.getUser(p.getUniqueId());
        if (user == null) return;

        double prev = user.getExp();
        user.addExp(amount);
        if (user.getExp() >= 100.0) {
            double over = user.getExp() - prev;
            user.setExp(over);
        }
    }
}
