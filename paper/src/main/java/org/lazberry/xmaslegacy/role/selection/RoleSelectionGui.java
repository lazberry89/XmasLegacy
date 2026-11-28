package org.lazberry.xmaslegacy.role.selection;

import org.lazberry.xmaslegacy.utils.ReadOnlyGui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.utils.*;

import java.util.ArrayList;
import java.util.List;

public class RoleSelectionGui implements ReadOnlyGui {
	private final Inventory inventory;
	private final XmasLegacy plugin;

	/** Marks which role an icon stands for, so the click listener reads it instead of the slot number. */
	public static final NamespacedKey ROLE_KEY = KeyUtils.get("role_select");
	private static final ServerRoles[] SELECTABLE = {
			ServerRoles.FARMER, ServerRoles.MINER, ServerRoles.FISHERMAN, ServerRoles.HUNTER, ServerRoles.BLACKSMITH
	};

	public RoleSelectionGui(XmasLegacy plugin) {
		this.plugin = plugin;
		this.inventory = Bukkit.createInventory(this, 9, ColorUtils.chat("&6&l직업선택"));

		var bg = InventoryHelper.background();
		for (int i = 0; i < inventory.getSize(); i++) inventory.setItem(i, bg);
		for (int i = 0; i < SELECTABLE.length; i++) inventory.setItem(2 + i, getIconByRole(SELECTABLE[i]));
	}

	/** Reads the role an icon was built for; null for anything else (background, player items). */
	public static @Nullable ServerRoles roleOf(@Nullable ItemStack item) {
		String value = KeyUtils.get(item, ROLE_KEY, PersistentDataType.STRING);
		return value == null ? null : ParseEnum.of(ServerRoles.class).parse(value);
	}

	private ItemStack getIconByRole(ServerRoles role) {
		var builder = switch (role) {
			case FARMER -> icon(Material.DIAMOND_HOE, "&a&l", role,
					"수확 시 추가 작물과 경험치를 얻습니다.",
					"심은 작물이 확률적으로 더 빨리 자랍니다.",
					"농사 땅을 밟아도 망가지지 않습니다.");
			case MINER -> icon(Material.DIAMOND_PICKAXE, "&7&l", role,
					"광물 채굴 시 추가 아이템과 경험치를 얻습니다.",
					"확률적으로 주변 광물을 빛나게 찾아냅니다.");
			case FISHERMAN -> icon(Material.FISHING_ROD, "&b&l", role,
					"낚은 생선이 확률적으로 구워져서 나옵니다.",
					"쓰레기는 돈으로 바뀌고, 보물은 즉시 팔 수 있습니다.",
					"입질이 오면 찌가 빛나며 알려줍니다.");
			case HUNTER -> icon(Material.DIAMOND_SWORD, "&c&l", role,
					"공격 시 30% 확률로 치명타가 터집니다.",
					"몬스터를 처치하면 돈과 경험치를 얻습니다.");
			case BLACKSMITH -> icon(Material.ANVIL, "&8&l", role,
					"전용 도구로 어디서든 강화창을 엽니다.",
					"강화할 때 경험치를 얻고, 10% 확률로 강화 정수를 얻습니다.");
			default -> throw new IllegalArgumentException("Not a selectable role: " + role);
		};
		return builder
				.setTag(ROLE_KEY, PersistentDataType.STRING, role.name())
				.hideAllFlags()
				.build();
	}

	private ItemBuilder icon(Material material, String color, ServerRoles role, String... passive) {
		return ItemBuilder.of(plugin, material)
				.name(ColorUtils.chat(color + role.getKor()).decoration(TextDecoration.ITALIC, false))
				.lore(getRoleLore(role.getSortedDescription().toArray(String[]::new), passive));
	}

	/**
	 * Item lore can't break lines inside one Component (a newline doesn't render there),
	 * so every argument becomes its own lore line.
	 * <pre>{@code
	 * .lore(getRoleLore(
	 *         new String[]{"농사 시 추가 경험치를 얻습니다.", "작물 판매 시 추가금을 받습니다."},
	 *         "수확 시 확률적으로 작물 2배"))
	 * }</pre>
	 */
	private List<Component> getRoleLore(String[] explanation, String... passive) {
		List<Component> lore = new ArrayList<>();
		lore.add(ColorUtils.chat("&6• 설명"));
		for (String line : explanation) lore.add(ColorUtils.chat("&7  " + line));
		lore.add(Component.empty());
		lore.add(ColorUtils.chat("&c• 능력"));
		for (String line : passive) lore.add(ColorUtils.chat("&7  " + line));
		return lore.stream().map(c -> c.decoration(TextDecoration.ITALIC, false))
				.toList();
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inventory;
	}
}
