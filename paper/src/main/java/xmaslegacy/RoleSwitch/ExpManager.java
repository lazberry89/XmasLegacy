package xmaslegacy.RoleSwitch;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;

public enum ExpManager {
	INSTANCE;

    private final UserManager um;

	ExpManager() {
        this.um = UserManager.INSTANCE;
    }

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
