package xmasLegacy.FirstRoleManager.Merchant;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.EconomyManager;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class MerchantStockInterface {
	private final Component title = ColorUtils.chat("&c&l상점 재고 관리");
	private final Component titleFarm = ColorUtils.chat("&c&l농부 재고");
	private final Component titleMiner = ColorUtils.chat("&c&l광부 재고");
	private @Nullable UUID owner;
	private final @NotNull Inventory inv;
	private final @NotNull Inventory farmInv;
	private final @NotNull Inventory minerInv;
	private final HashMap<Material, Integer> stock = new HashMap<>();
	private final EconomyManager em;
	private final XmasLegacy plugin;

	public void setOwner(Player p) {
		this.owner = p.getUniqueId();
	}
	public void setOwner(UUID uuid) {
		this.owner = uuid;
	}
	public @Nullable UUID getOwner() {
		return this.owner;
	}
	private static MerchantStockInterface instance;

	public static MerchantStockInterface getInstance() {
		if (instance == null) instance = new MerchantStockInterface();
		return instance;
	}

	private MerchantStockInterface() {
		this.plugin = XmasLegacy.getInstance();
		this.em = EconomyManager.INSTANCE;
		//Main
		this.inv = Bukkit.createInventory(null, 9, title);
		ItemStack bg = ItemBuilder.of(plugin, Material.BLACK_STAINED_GLASS_PANE)
				.setName(ColorUtils.chat(""))
				.setLore(ColorUtils.chat(""))
				.hideAllFlags()
				.build();
		for (int i = 0; i < this.inv.getSize(); i++) this.inv.setItem(i, bg);
		ItemStack forFarmer = ItemBuilder.of(plugin, Material.IRON_HOE)
				.setName(ColorUtils.chat("&f&l농부 수급품"))
				.setLore(ColorUtils.chat("&7농부가 시스템 상점에 판매한 아이템을 확인할 수 있어요."))
				.hideAllFlags()
				.setGlint(true)
				.build();
		ItemStack forMiner = ItemBuilder.of(plugin, Material.GOLDEN_PICKAXE)
				.setName(ColorUtils.chat("&e&l광부 수급품"))
				.setLore(ColorUtils.chat("&7광부가 시스템 상점에 판매한 아이템을 확인할 수 있어요."))
				.hideAllFlags()
				.setGlint(true)
				.build();
		this.inv.setItem(2, forFarmer);
		this.inv.setItem(6, forMiner);

		//Farmer
		this.farmInv = Bukkit.createInventory(null, 9, titleFarm);
		for (int i = 0; i < this.farmInv.getSize(); i++) this.farmInv.setItem(i, bg);
		ItemStack wheat = ItemBuilder.of(plugin, Material.WHEAT)
				.setName(ColorUtils.chat("&6&l밀"))
				.setLore(ColorUtils.chat("&7농부가 시스템 상점에 판매한 밀이에요."), ColorUtils.chat(String.format("&f현재수량 : %d", getStock(Material.WHEAT))))
				.hideAllFlags()
				.build().clone();
		ItemStack sunflower = ItemBuilder.of(plugin, Material.TORCHFLOWER)
				.setName(ColorUtils.chat("&6&l태양초"))
				.setLore(ColorUtils.chat("&7농부가 시스템 상점에 판매한 태양초에요."), ColorUtils.chat(String.format("&f현재수량 : %d", getStock(Material.TORCHFLOWER))))
				.hideAllFlags()
				.build().clone();
		ItemStack waiting = ItemBuilder.of(plugin, Material.BARRIER)
				.setName(ColorUtils.chat("&c&l준비중..."))
				.setLore(ColorUtils.chat("&7아직 준비중인 수급품이에요."))
				.hideAllFlags()
				.build().clone();
		ItemStack goBack = ItemBuilder.of(plugin, Material.IRON_HOE)
				.setName(ColorUtils.chat("&c&l뒤로가기"))
				.setLore(ColorUtils.chat("&7상점 재고 관리 화면으로 돌아가요."))
				.hideAllFlags()
				.build().clone();
		for (int i = 0; i < this.farmInv.getSize() - 1; i++) this.farmInv.setItem(i, waiting);
		this.farmInv.setItem(8, goBack);
		this.farmInv.setItem(0, wheat);
		this.farmInv.setItem(1, sunflower);

		//Miner
		this.minerInv = Bukkit.createInventory(null, 9, titleMiner);
		for (int i = 0; i < this.minerInv.getSize() - 1; i++) this.minerInv.setItem(i, waiting);
		ItemStack coal = ItemBuilder.of(plugin, Material.COAL)
				.setName(ColorUtils.chat("&8&l석탄"))
				.setLore(ColorUtils.chat("&7광부가 시스템 상점에 판매한 석탄이에요."), ColorUtils.chat(String.format("&f현재수량 : %d", getStock(Material.COAL))))
				.hideAllFlags()
				.build().clone();
		ItemStack iron = ItemBuilder.of(plugin, Material.IRON_INGOT)
				.setName(ColorUtils.chat("&7&l철"))
				.setLore(ColorUtils.chat("&7광부가 시스템 상점에 판매한 철이에요."), ColorUtils.chat(String.format("&f현재수량 : %d", getStock(Material.IRON_INGOT))))
				.hideAllFlags()
				.build().clone();
		ItemStack gold = ItemBuilder.of(plugin, Material.GOLD_INGOT)
				.setName(ColorUtils.chat("&e&l금"))
				.setLore(ColorUtils.chat("&7광부가 시스템 상점에 판매한 금이에요."), ColorUtils.chat(String.format("&f현재수량 : %d", getStock(Material.GOLD_INGOT))))
				.hideAllFlags()
				.build().clone();
		ItemStack diamond = ItemBuilder.of(plugin, Material.DIAMOND)
				.setName(ColorUtils.chat("&b&l다이아몬드"))
				.setLore(ColorUtils.chat("&7광부가 시스템 상점에 판매한 다이아몬드에요."), ColorUtils.chat(String.format("&f현재수량 : %d", getStock(Material.DIAMOND))))
				.hideAllFlags()
				.build().clone();
		this.minerInv.setItem(8, goBack);
		this.minerInv.setItem(0, coal);
		this.minerInv.setItem(1, iron);
		this.minerInv.setItem(2, gold);
		this.minerInv.setItem(3, diamond);
	}

	public void OpenStock(Player view) {
		view.openInventory(this.inv);
		view.updateInventory();
		view.playSound(view, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
	}

	public void OpenFarmer(Player view) {
		view.openInventory(this.farmInv);
		view.updateInventory();
		view.playSound(view, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
	}

	public void OpenMiner(Player view) {
		view.openInventory(this.minerInv);
		view.updateInventory();
		view.playSound(view, Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
	}

	public void addStock(Material material, int amount) {
		this.stock.put(material, this.stock.getOrDefault(material, 0) + amount);
	}

	public int getStock(Material material) {
		return this.stock.getOrDefault(material, 0);
	}

	public @NotNull Inventory getInventory() {
		return this.inv;
	}

	public void Submit(Material material) {
		int count = this.stock.getOrDefault(material, 0);
		this.stock.put(material, 0);

		if (em.deposit(getOwner(), count * 2)) {
			plugin.getSLF4JLogger().info("상점에서 정상적으로 상점 주인에게 돈을 입금하였습니다.");
		} else {
			plugin.getSLF4JLogger().warn("상점에게 입금을 실패하였습니다.");
		}
	}

	public @NotNull Component getTitle() {return this.title;}
	public @NotNull Component getTitleFarm() {return this.titleFarm;}
	public @NotNull Component getTitleMiner() {return this.titleMiner;}
}
