package xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.EconomyManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PriestShopManager {
	private final Map<UUID, PriestShop> shops = new ConcurrentHashMap<>();

	public PriestShopManager() {
	}

	public PriestShop getOrCreate(Player owner) {
		return shops.computeIfAbsent(owner.getUniqueId(),
				uuid -> new PriestShop(owner));
	}

	public PriestShop get(UUID uuid) {
		return shops.get(uuid);
	}

	public void remove(UUID uuid) {
		shops.remove(uuid);
	}
}
