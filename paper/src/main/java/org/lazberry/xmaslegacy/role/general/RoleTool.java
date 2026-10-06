package org.lazberry.xmaslegacy.role.general;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.roles.HiddenRoles;
import org.lazberry.xmaslegacy.roles.Role;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.utils.ParseEnum;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

@Registry.Exclude(type = ServerType.LOBBY)
public class RoleTool {
	public static final NamespacedKey key = KeyUtils.get("role");

	public static boolean isTool(ItemStack item, Consumer<ServerRoles> found) {
		String value = KeyUtils.get(item, key, PersistentDataType.STRING);
		if (value == null) return false;

		ServerRoles role = ParseEnum.of(ServerRoles.class).parse(value);
		if (role == null) return false;

		found.accept(role);
		return true;
	}

    private final Map<ServerRoles, ItemStack> roleTool = new EnumMap<>(ServerRoles.class);
	private final Map<HiddenRoles, ItemStack> hiddenRoleTool = new EnumMap<>(HiddenRoles.class);

	@Inject
	public RoleTool(XmasLegacy plugin) {
		roleTool.put(ServerRoles.FISHERMAN, fishermanTool(plugin));
		roleTool.put(ServerRoles.BLACKSMITH, blackSmithTool(plugin));
	}

	private ItemStack fishermanTool(XmasLegacy plugin) {
		return ItemBuilder.of(plugin, Material.FISHING_ROD)
				.name(ColorUtils.chat("&b강태공&f의 낚시대"))
				.lore(ColorUtils.chat("&7어부가 사용하는 낚시대입니다."),
						ColorUtils.chat("확률적으로 낚은 아이템을 변환해줍니다."))
				.hideAllFlags()
				.setUnbreakable()
				.setTag(key, PersistentDataType.STRING, "FISHERMAN")
				.addEnchant(Enchantment.LURE, 3)
				.build();
	}

	private ItemStack blackSmithTool(XmasLegacy plugin) {
		return ItemBuilder.of(plugin, Material.ANVIL)
				.name(ColorUtils.chat("&8대장장이&f의 대장간"))
				.lore(ColorUtils.chat(""),
						ColorUtils.chat(""))
				.hideAllFlags()
				.setTag(key, PersistentDataType.STRING, "BLACKSMITH")
				.glint(true)
				.build();
	}

	public ItemStack getRoleItem(Role role) {
		if (role instanceof ServerRoles sr) return roleTool.get(sr);
		else return hiddenRoleTool.get((HiddenRoles) role);
	}
}
