package org.lazberry.xmaslegacy.ServerPrefix;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.CustomPrefix;
import org.lazberry.xmaslegacy.settings.Initiator;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.HashMap;
import java.util.Map;

@Registry.Exclude(type = ServerType.LOBBY)
public class PrefixManager implements Initiator {
	private final @NotNull UserManager um;
	private final @NotNull Map<String, CustomPrefix> customPrefix = new HashMap<>();

	@Inject
	public PrefixManager(@NotNull UserManager um) {
		this.um = um;
	}

	public boolean add(@NotNull Player p, @NotNull ServerPrefix prefix) {
		User user = um.getUser(p.getUniqueId());
		if (user == null) return false;
		return user.addPrefix(prefix);
	}

	public @NotNull CustomPrefix create(@NotNull String id, @NotNull Component prefix) {
		if (this.customPrefix.containsKey(id)) return this.customPrefix.get(id);
		CustomPrefix newPrefix = new CustomPrefix(id, prefix);
		this.customPrefix.put(id, newPrefix);

		return newPrefix;
	}

	public boolean remove(@NotNull String id) {
		if (!this.customPrefix.containsKey(id)) return false;

		var prx = this.customPrefix.get(id);
		um.getUsers().forEach(u -> {
			if (prx.equals(u.getEquipPrefix())) u.removeEquipped();
			if (u.getAvailablePrefix().contains(prx)) u.removePrefix(prx);
		});

		this.customPrefix.remove(id);
		return true;
	}

	public void removePrefixIfNotValid(@NotNull User user) {
		ServerPrefix equipped = user.getEquipPrefix();
		if (equipped instanceof CustomPrefix cp)
			if (!this.customPrefix.containsKey(cp.name()))
				user.removeEquipped();

		user.getAvailablePrefix().removeIf(prefix ->
				prefix instanceof CustomPrefix cp && !this.customPrefix.containsKey(cp.name())
		);
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
