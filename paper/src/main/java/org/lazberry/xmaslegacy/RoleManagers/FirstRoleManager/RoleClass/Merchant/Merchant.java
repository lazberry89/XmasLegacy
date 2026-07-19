package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Merchant;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Merchant.Skill.OpenStocks;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.RoleClass.Merchant.Skill.SellItems;
import org.lazberry.xmaslegacy.RoleManagers.RoleContainer;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Roles;
import org.lazberry.xmaslegacy.Emblems.EmblemType;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.Utils.Config;
import org.lazberry.xmaslegacy.Utils.ItemBuilder;
import org.lazberry.xmaslegacy.Utils.ParseItem;

@Roles
public class Merchant extends AbstractFirstRole {
	private Material weapon_item;
	private Material armor_item;

	private Container container;

	private final @NotNull OpenStocks open = new OpenStocks();
	private final @NotNull SellItems sell = new SellItems();

	public Merchant() {
		super(BasicRoles.MERCHANT);
		this.loadRoleData(getRole().name().toLowerCase());
	}

	public record Container(
			PriceManager priceManager,
			MerchantStockInterface stockInterface
	) implements RoleContainer {}

	@Override
	protected void loadCustomStats(@NotNull FileConfiguration config) {
		var configs = Config.of(config);
		configs.setDefault("tool.role_weapon", "ENDER_CHEST")
				.setDefault("tool.role_armor", "IRON_HELMET");

		this.weapon_item = ParseItem.parse(configs.getValue("tool.role_weapon"), Material.ENDER_CHEST);
		this.armor_item = ParseItem.parse(configs.getValue("tool.role_armor"), Material.IRON_HELMET);

		this.container = new Container(
				PriceManager.INSTANCE,
				MerchantStockInterface.INSTANCE
		);
	}

	@Override
	public void useFirstSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.TARGET, open, container, getCooldown1());
	}

	@Override
	public void useSecondSkill(@NonNull Player p) {
		handleSkill(p, emblem, EmblemType.RANGE, sell, container, getCooldown2());
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
