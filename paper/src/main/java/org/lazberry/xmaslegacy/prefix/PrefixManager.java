package org.lazberry.xmaslegacy.prefix;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;

@Registry.Exclude(type = ServerType.LOBBY)
public class PrefixManager implements Initiator {
	private final @NotNull UserManager um;

	@Inject
	public PrefixManager(@NotNull UserManager um) {
		this.um = um;
	}

	public boolean add(@NotNull Player p, @NotNull ServerPrefix prefix) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		return user.addPrefix(prefix);
	}

	public boolean remove(@NotNull Player p, @NotNull ServerPrefix prefix) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		return user.removePrefix(prefix);
	}

	@CanIgnoreReturnValue
	public boolean equip(@NotNull Player p, @NotNull ServerPrefix prefix) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		if (!user.getAvailablePrefix().contains(prefix)) return false;
		user.setEquipPrefix(prefix);
		return true;
	}

	@CanIgnoreReturnValue
	public boolean unequip(@NotNull Player p) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		return user.removeEquipped();
	}

	@Override
	public void init() {

	}
}
