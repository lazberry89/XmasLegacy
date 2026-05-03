package xmasLegacy.FirstRoleManager.Merchant;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.User.User;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PriceInterface {
	private final Inventory priceInv;
	private final Inventory shopInv;
	private final Map<Integer, Product> shopItem = new HashMap<>();
	private Integer selectedSlot;
	ItemStack bg = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.GRAY_STAINED_GLASS_PANE)
			.setName(ColorUtils.chat(""))
			.setLore(ColorUtils.chat(""))
			.hideAllFlags()
			.build();

	public PriceInterface() {
		this.priceInv = Bukkit.createInventory(null, 9, Constants.PRICE_TITLE);
		this.shopInv = Bukkit.createInventory(null, 54, Constants.SHOP_TITLE);
	}

	public void setProduct(@NotNull Product product, int price) {
		product.setPrice(price);
		shopItem.put(selectedSlot, product);
	}

	public void removeProduct() {
		shopItem.remove(selectedSlot);
	}

	public @Nullable Product getProduct(int slot) {
		return shopItem.get(slot);
	}

	public Integer getSelectedSlot() {
		return this.selectedSlot;
	}

	public void setSlot(Integer selectedSlot) {
		this.selectedSlot = selectedSlot;
	}

	public Inventory PriceSet() {
		if (selectedSlot == null) return MerchantShop();
		Product prd =  getProduct(getSelectedSlot());
		ItemStack priceUp = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.RED_STAINED_GLASS_PANE)
				.setName(ColorUtils.chat("&c&l가격 올리기"))
				.setLore(ColorUtils.chat("&7상품의 가격에서 &6+500&7을 추가합니다."), ColorUtils.chat(String.format("현재가격: &6&l%s", prd == null ? "-" :  prd.getPrice())))
				.hideAllFlags()
				.build().clone();
		ItemStack priceDown = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.BLUE_STAINED_GLASS_PANE)
				.setName(ColorUtils.chat("&9&l가격 내리기"))
				.setLore(ColorUtils.chat("&7상품의 가격에서 &6500&7을 차감합니다."), ColorUtils.chat(String.format("현재가격: &6&l%s", prd == null ? "-" : prd.getPrice())))
				.hideAllFlags()
				.build().clone();
		ItemStack done = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.LIME_STAINED_GLASS_PANE)
				.setName(ColorUtils.chat("&a&l등록하기"))
				.setLore(ColorUtils.chat("&7상품을 등록합니다."), ColorUtils.chat(String.format("&7판매가격: %s", prd == null ? "-" : prd.getPrice())))
				.hideAllFlags()
				.build().clone();
		ItemStack back = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.BARRIER)
				.setName(ColorUtils.chat("&c&l뒤로가기"))
				.setLore(ColorUtils.chat("&7클릭하여 상점메뉴로 돌아갑니다."))
				.hideAllFlags()
				.build().clone();
		for (int i = 0; i < this.priceInv.getSize(); i++) {
			if (i == 4) continue;
			this.priceInv.setItem(i, bg);
		}
		this.priceInv.setItem(0, back);
		this.priceInv.setItem(3, priceUp);
		this.priceInv.setItem(5, priceDown);
		this.priceInv.setItem(8, done);

		return this.priceInv;
	}

	public Inventory MerchantShop() {
		ItemStack noProduct = ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.BARRIER)
				.setName(ColorUtils.chat("&c&l등록된 상품이 없습니다!"))
				.setLore(ColorUtils.chat("&7상점 주인장이 좋은걸 언젠간 올려 주겠죠?"))
				.hideAllFlags()
				.build().clone();


		for (int i = 0; i < shopInv.getSize(); i++) shopInv.setItem(i, bg);
		List<Integer> shopPattern = new ArrayList<>(List.of(10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34));
		shopPattern.forEach(s -> {
			Product prd = getProduct(s);
			this.shopInv.setItem(s, prd == null ? noProduct : prd.getItem());
		});

		return this.shopInv;
	}

	public List<Integer> getAvailableSlot() {
		return new ArrayList<>(List.of(10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34));
	}
}
