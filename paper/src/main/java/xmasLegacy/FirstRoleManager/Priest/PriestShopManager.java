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
	private static PriestShopManager instance;

	public static PriestShopManager getInstance() {
		if (instance == null) {
			instance = new PriestShopManager();
		}
		return instance;
	}

	private PriestShopManager() {
		this.CDI = ConductableItems.getInstance();
		this.EM = EconomyManager.getInstance();
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
