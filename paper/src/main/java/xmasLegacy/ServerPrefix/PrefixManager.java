package xmasLegacy.ServerPrefix;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import xmasLegacy.XmasLegacy;

public class PrefixManager {
	private final XmasLegacy plugin;
	private final UserManager um;

	private static PrefixManager instance;

	private PrefixManager() {
		this.plugin = XmasLegacy.getInstance();
		this.um = UserManager.getInstance();
	}

	public static PrefixManager getInstance() {
		if (instance == null) {
			instance = new PrefixManager();
		}
		return instance;
	}

	public boolean addPrefix(@NotNull Player p, @NotNull ServerPrefix prefix) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		return user.addPrefix(prefix);
	}

	public boolean removePrefix(@NotNull Player p, @NotNull ServerPrefix prefix) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		return user.removePrefix(prefix);
	}

	@CanIgnoreReturnValue
	public boolean equipPrefix(@NotNull Player p, @NotNull ServerPrefix prefix) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		if (!user.getAvailablePrefix().contains(prefix)) return false;
		user.setEquipPrefix(prefix);
		return true;
	}

	@CanIgnoreReturnValue
	public boolean unequipPrefix(@NotNull Player p) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		return user.removeEquipped();
	}
}
