package xmasLegacy.FirstRoleManager.Priest;

import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.EconomyManager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PriestShopManager {
	private final Map<UUID, PriestShop> shops = new ConcurrentHashMap<>();
	private final ConductableItems CDI;
	private final EconomyManager EM;

	public PriestShopManager(ConductableItems CDI, EconomyManager EM) {
		this.CDI = CDI;
		this.EM = EM;
	}

	public PriestShop getOrCreate(Player owner) {
		return shops.computeIfAbsent(owner.getUniqueId(),
				uuid -> new PriestShop(CDI, EM, owner));
	}

	public PriestShop get(UUID uuid) {
		return shops.get(uuid);
	}

	public void remove(UUID uuid) {
		shops.remove(uuid);
	}
}
