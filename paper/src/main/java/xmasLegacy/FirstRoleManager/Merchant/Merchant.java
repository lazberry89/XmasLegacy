package xmasLegacy.FirstRoleManager.Merchant;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.settings.BasicSkills;
import xmasLegacy.FirstRoleManager.AbstractFirstRole;
import xmasLegacy.Utils.ItemBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("DuplicatedCode")
public class Merchant extends AbstractFirstRole {
	private final Map<UUID, BasicSkills> currentSkill = new HashMap<>();
	public BasicSkills getCurrentSkill(Player p) {return currentSkill.getOrDefault(p.getUniqueId(), BasicSkills.OPEN_STOCKS);}
	public void next(Player p) {currentSkill.put(p.getUniqueId(), getCurrentSkill(p).next());}
	private final PriceInterface PIF;
	private final MerchantStockInterface MSI;
	private Material weapon_item;
	private Material armor_item;

	private static Merchant instance;

	public static Merchant getInstance() {
		if (instance == null) instance = new Merchant();
		return instance;
	}

	private Merchant() {
		super(Roles.MERCHANT);
		this.PIF = PriceInterface.getInstance();
		this.MSI = MerchantStockInterface.getInstance();
		this.loadRoleData(getRole().name().toLowerCase());
	}

	@Override
	protected void loadCustomStats(FileConfiguration config) {
		config.addDefault("tool.role_weapon", "ENDER_CHEST");
		config.addDefault("tool.role_armor", "IRON_HELMET");

		Material weapon;
		try {
			weapon = Material.valueOf(config.getString("tool.role_weapon"));
		} catch (IllegalArgumentException e) {
			weapon = Material.ENDER_CHEST;
		}
		this.weapon_item = weapon;

		Material armor;
		try {
			armor = Material.valueOf(config.getString("tool.role_armor"));
		} catch (IllegalArgumentException e) {
			armor = Material.IRON_HELMET;
		}
		this.armor_item = armor;
	}

	@Override
	public void useFirstSkill(Player p) {
		ItemStack tool = p.getInventory().getHelmet();
		if (tool == null || tool.getType().isAir()) return;
		MSI.setOwner(p);
		MSI.OpenStock(p);
		p.playSound(p, Sound.ENTITY_VILLAGER_WORK_CARTOGRAPHER, 1.0f, 1.0f);
	}

	@Override
	public void useSecondSkill(Player p) {
		ItemStack tool = p.getInventory().getItemInMainHand();
		if (tool.getType().isAir()) return;
		p.openInventory(PIF.MerchantShop());
		PIF.setOwner(p.getUniqueId());
		p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
	}

	@Override
	public @NotNull Roles getRole() {
		return Roles.MERCHANT;
	}

	@Override
	public @NotNull ItemStack roleWeapon() {
		return ItemBuilder.of(getPlugin(), this.weapon_item)
				.setName(ColorUtils.chat("&d&l상인의 보자기"))
				.setLore(ColorUtils.chat("&7상점을 열거나 매입품을 확인할 수 있어요."))
				.hideAllFlags()
				.setTag("role_id", "merchant")
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleArmor() {
		return ItemBuilder.of(getPlugin(), this.armor_item)
				.setName(ColorUtils.chat("&d&l상인대가리 보호막"))
				.setLore(ColorUtils.chat("&7상인한테 과연 이런게 필요할까?"), ColorUtils.chat("&7아 근데 스킬쓸려면 필요함 ㅇㅇ"))
				.hideAllFlags()
				.setTag("role_id", "merchant")
				.build().clone();
	}

	@Override
	public @NotNull ItemStack roleBook() {
		String page1 = """
          &0&l[ &4&l상인 가이드 &0&l ]&r
          
          &0상인은 마을 전체의 경제를 쥐고 흔들며,
          &0농부, 광부의 생산품을 매입하고 판매합니다.
          &7누구보다 비밀이 많은 직업입니다..
		
          &7&m-----------------
          &0&l[ &1&l전직 계보 &0&l ]&r
          &0- &82차 전직: &8&o..?
          &0- &83차 전직: &8&o..?
          """;

		String page2 = String.format("""
          &0&l[ &2&l보유 스킬 &0&l ]&r
          
          &4&l▶ &0&l재고 확인 &8[%d초]
          &0매입한 상품들을 확인하고 시스템에
          &0제출하여 수입을 챙길 수 있습니다.
          
          &4&l▶ &0&l상품판매 &8[%d초]
          &0공식적으로 아이템을
          &0판매하여 수익을 얻을 수 있습니다.
          &7&m-----------------
          """, getCooldown1(), getCooldown2());
		return createGuideBook("상인", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", page1, page2);
	}
}
