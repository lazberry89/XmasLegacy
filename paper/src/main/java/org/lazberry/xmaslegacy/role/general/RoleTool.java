package org.lazberry.xmaslegacy.role.general;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Roles.HiddenRoles;
import org.lazberry.xmaslegacy.Roles.ServerRoles;
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
	private final XmasLegacy plugin;
	private final RoleManager rm;

	@Inject
	public RoleTool(XmasLegacy plugin, RoleManager rm) {
		this.plugin = plugin;
		this.rm = rm;
	}

	private ItemStack farmerTool() {
		return ItemBuilder.of(plugin, Material.DIAMOND_HOE)
				.setName(ColorUtils.chat(""))
				.setLore(ColorUtils.chat())
				.hideAllFlags()
				.addEnchant()
	}
}
