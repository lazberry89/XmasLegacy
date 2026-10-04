package org.lazberry.xmaslegacy.shop.showcase;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.shop.goods.Goods;
import org.lazberry.xmaslegacy.shop.goods.GoodsConfig;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InventoryHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Registry.Exclude(type = ServerType.LOBBY)
public class ShowCaseManager implements Initiator {
	private final Map<String, ShowCase> showCase = new ConcurrentHashMap<>();
	private final Map<String, Goods> goods = new ConcurrentHashMap<>();
	private final UserManager userManager;
	private final GoodsConfig goodsConfig;
	private final ShowCaseConfig caseConfig;

	@Inject
	public ShowCaseManager(UserManager userManager, GoodsConfig goodsConfig, ShowCaseConfig caseConfig) {
		this.userManager = userManager;
		this.goodsConfig = goodsConfig;
		this.caseConfig = caseConfig;
	}

	@Override
	public void init() {
		cleanUp();
		goods.putAll(goodsConfig.loadSync());
		showCase.putAll(caseConfig.loadSync(goods));
	}

	private void cleanUp() {
		showCase.forEach((key, showCase) -> showCase.cleanUp());
		showCase.clear();
		goods.clear();
	}

	public Collection<ShowCase> getShowCases() {
		return Collections.unmodifiableCollection(showCase.values());
	}

	public Collection<Goods> getGoods() {
		return Collections.unmodifiableCollection(goods.values());
	}

	public void registerGoods(String name, ItemStack item, int initial) {
		goods.putIfAbsent(name, new Goods(name, item, initial));
	}

	public boolean removeGoods(String name) {
		if (goods.containsKey(name)) {
			goods.remove(name);

			var show = showCase.remove(name);
			if (show != null) show.cleanUp();
			return true;
		}
		return false;
	}

	public boolean removeShowCase(String name) {
		var s = showCase.remove(name);
		if (s != null) {
			s.cleanUp();
			return true;
		}
		return false;
	}

	public boolean spawnShowCase(String id, Location location, BlockFace face) {
		if (showCase.containsKey(id)) return false;

		Goods g = goods.get(id);
		if (g == null) return false;

		ShowCase s = new ShowCase(g);
		s.spawnShowcase(location);
		s.spawnWallButton(location, face);

		showCase.put(id, s);
		return true;
	}

	public boolean purchase(Player player, String clicked) {
		ShowCase showCase = this.showCase.get(clicked);
		if (showCase == null) return false;

		Goods g = showCase.getGoods();
		int price = g.getPrice();

		if (userManager.withdraw(player.getUniqueId(), price)) {
			InventoryHelper.giveItemOrKeep(player, g.getItem());
			return true;
		}
		return false;
	}

	@Override
	public void close() {
		caseConfig.saveSync(showCase);
		goodsConfig.saveSync(goods);
		cleanUp();
	}
}
