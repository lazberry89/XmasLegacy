package xmasLegacy.Region;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import org.lazberry.xmaslegacy.User.UserManager;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

@SuppressWarnings("ClassCanBeRecord")
public class RegionIndicator implements Listener {
	private final RegionManager RM;
	private final UserManager UM;
	private final XmasLegacy plugin;

	public RegionIndicator(RegionManager RM, UserManager UM, XmasLegacy plugin) {
		this.RM = RM;
		this.UM = UM;
		this.plugin = plugin;
	}

	public static @NotNull ItemStack RegionBeacon() {
		return ItemBuilder.of(JavaPlugin.getPlugin(XmasLegacy.class), Material.BEACON)
				.setName(ColorUtils.chat("&b&l구역 생성기"))
				.setLore(ColorUtils.chat("&8이 장치를 설치한 뒤 상호작용하여 구역을 생성하세요!"))
				.setTag("RegionBeacon", "user")
				.setGlint(true)
				.build()
				.clone();
	}

	@EventHandler
	public void onBeaconPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		ItemStack tool = e.getItemInHand();
		ItemMeta meta = tool.getItemMeta();
		NamespacedKey key = new NamespacedKey(plugin, "RegionBeacon");

		if (meta == null) return;
		String pdc = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);

		if (pdc != null && pdc.equals("user")) {
			Location loc = e.getBlock().getLocation();
			if (UM.getUser(p.getUniqueId()) == null) {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 유저 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요."));
				e.setCancelled(true);
				return;
			}

			if (RM.getRegionAt(loc) != null) {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 이곳은 이미 누군가의 구역입니다!"));
				e.setCancelled(true);
				return;
			}

			Region newRegion = new Region(p, loc, UM);

			boolean isOverlapping = RM.getRegions().stream().anyMatch(r -> r.overlaps(newRegion));
			if (isOverlapping) {
				p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 다른 구역에 겹치는 구역이 있습니다!"));
				e.setCancelled(true);
				return;
			}
			RM.addRegion(p, newRegion);
			p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 구역을 생성했습니다. &6ID: " + newRegion.getId()));
		}
	}

	@EventHandler
	public void onBeaconBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		Block block = e.getBlock();
		Region region = RM.getRegionAt(block.getLocation());
		if (block.getType() != Material.BEACON || region == null) return;
		if (!region.getOwner().equals(p.getUniqueId())) return;

		if (!region.getCenter().equals(block.getLocation())) return;
		RM.removeRegion(region);
		p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 구역을 삭제했습니다. &6ID: " + region.getId()));
	}
}
