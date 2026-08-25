package org.lazberry.xmaslegacy.role.general;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.roles.HiddenRoles;
import org.lazberry.xmaslegacy.roles.Role;
import org.lazberry.xmaslegacy.roles.ServerRoles;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ItemBuilder;

import java.util.EnumMap;
import java.util.Map;

@Registry.Include(type = ServerType.GLOBAL)
public class RoleTool {
    private final Map<ServerRoles, ItemStack> roleTool = new EnumMap<>(ServerRoles.class);
	private final Map<HiddenRoles, ItemStack> hiddenRoleTool = new EnumMap<>(HiddenRoles.class);
	private final RoleManager rm;

	@Inject
	public RoleTool(XmasLegacy plugin, RoleManager rm) {
		this.rm = rm;
		roleTool.put(ServerRoles.FISHERMAN, fishermanTool(plugin));
	}

	private ItemStack fishermanTool(XmasLegacy plugin) {
		return ItemBuilder.of(plugin, Material.FISHING_ROD)
				.setName(ColorUtils.chat("&b강태공&f의 낚시대"))
				.setLore(ColorUtils.chat("&7어부가 사용하는 낚시대입니다."),
						ColorUtils.chat("확률적으로 낚은 아이템을 변환해줍니다."))
				.hideAllFlags()
				.setUnbreakable()
				.addEnchant(Enchantment.LUCK_OF_THE_SEA, 3)
				.addEnchant(Enchantment.LURE, 3)
				.build();
	}

	public ItemStack getRoleItem(Role role) {
		if (role instanceof ServerRoles sr) return roleTool.get(sr);
		else return hiddenRoleTool.get((HiddenRoles) role);
	}
}
