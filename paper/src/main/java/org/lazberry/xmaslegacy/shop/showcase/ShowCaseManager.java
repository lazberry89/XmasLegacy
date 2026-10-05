package org.lazberry.xmaslegacy.shop.showcase;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.shop.goods.Goods;
import org.lazberry.xmaslegacy.shop.goods.GoodsConfig;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.KeyUtils;

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
	private TextDisplay textDisplay;

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

	public void sendInfo(Player player, String id) {
		Goods g = goods.get(id);
		if (g == null) {
			InfoUtils.error(player, "존재하지 않는 상품입니다.");
			return;
		}
		String msg = String.format("""
          
          
          
          
          
          
          
          &7&m----------------------------------
          &f&l[ &e&lSHOP &f&l] &7상품 정보
          
          &f▪ 상품명 : &b%s
          &f▪ 가   격 : &a%,d원
          &7&m----------------------------------""", g.getName(), g.getPrice());
		player.sendMessage(ColorUtils.chat(msg));
		player.playSound(player, Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
	}

	public void removeInfoDisplay(Entity entity) {
		if (entity == null || !entity.isValid()) {
			if (this.textDisplay != null) {
				this.textDisplay.remove();
				this.textDisplay = null;
			}
		} else if (KeyUtils.hasKey(entity, KeyUtils.get("info_display"))) {
			entity.remove();
			this.textDisplay = null;
		}
	}

	public TextDisplay spawnInfoTextDisplay(Player player, Location targetLoc) {
		Location spawnLoc = targetLoc.clone();
		spawnLoc.setYaw(player.getLocation().getYaw() + 180f);
		spawnLoc.setPitch(0f);

		String text = """
        &c&l🎄 &e&lCHRISTMAS HIDDEN SHOP &c&l🎄
        &f&l[ 크리스마스 히든 치장품 상점 ]
        
        &f▪ &b&l우클릭 &7: &f상품 정보 & 미리보기
        &f▪ &c&lShift + 우클릭 &7: &f상품 즉시 구매
        
        &7&o※ 성탄절 한정 상품이 판매 중입니다.
        """;

		return spawnLoc.getWorld().spawn(spawnLoc, TextDisplay.class, display -> {
			display.text(ColorUtils.chat(text));
			display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
			display.setBillboard(TextDisplay.Billboard.FIXED);
			display.setShadowed(true);
			display.setBrightness(new Display.Brightness(15, 15));
			display.setSeeThrough(false);

			KeyUtils.set(display, KeyUtils.get("info_display"), true);
			display.setLineWidth(200);
			display.setPersistent(true);
			this.textDisplay = display;
		});
	}

	@Override
	public void close() {
		caseConfig.saveSync(showCase);
		goodsConfig.saveSync(goods);
		cleanUp();
	}
}
