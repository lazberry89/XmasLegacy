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

	public PrefixManager(XmasLegacy plugin) {
		this.plugin = plugin;
		this.um = plugin.UM;
	}

	@CanIgnoreReturnValue
	public int addPrefix(@NotNull Player p, @NotNull ServerPrefix prefix) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return -1;
		user.addPrefix(prefix);
		return user.getAvailablePrefix().size();
	}

	public boolean removePrefix(@NotNull Player p, @NotNull ServerPrefix prefix) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		if (user.getAvailablePrefix().contains(prefix)) {
			user.removePrefix(prefix);
			return true;
		}
		return false;
	}

	public boolean equipPrefix(@NotNull Player p, @NotNull ServerPrefix prefix) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		if (!user.getAvailablePrefix().contains(prefix)) return false;
		user.setEquipPrefix(prefix);
		return true;
	}

	public boolean unequipPrefix(@NotNull Player p) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		user.setEquipPrefix(null);
		return true;
	}
}
