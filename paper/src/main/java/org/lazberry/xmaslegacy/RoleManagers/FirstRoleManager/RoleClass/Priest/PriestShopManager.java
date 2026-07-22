package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Priest;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Registry
public class PriestShopManager {
	private final Map<UUID, PriestShop> shops = new ConcurrentHashMap<>();

	public PriestShopManager() {}

	public @NotNull PriestShop getOrCreate(@NotNull Player owner) {
		return shops.computeIfAbsent(owner.getUniqueId(),
				uuid -> new PriestShop(owner));
	}

	public @Nullable PriestShop get(@NotNull UUID uuid) {
		return shops.get(uuid);
	}

	public void remove(@NotNull UUID uuid) {
		shops.remove(uuid);
	}
}
