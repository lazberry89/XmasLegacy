package xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public enum PriestShopManager {
	INSTANCE;

	private final Map<UUID, PriestShop> shops = new ConcurrentHashMap<>();

	PriestShopManager() {
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
