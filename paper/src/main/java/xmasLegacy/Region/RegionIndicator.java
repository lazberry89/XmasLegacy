package xmasLegacy.Region;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.User.UserManager;
import xmasLegacy.InfoLevel;
import xmasLegacy.ServerPrefix.UserTagManager;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

public class RegionIndicator implements Listener {
	private final RegionManager rm;
	private final UserManager um;
	private final XmasLegacy plugin;

	public RegionIndicator() {
		this.rm = RegionManager.getInstance();
		this.um = UserManager.getInstance();
		this.plugin = XmasLegacy.getInstance();
	}

	@EventHandler
	public void UserRegionCreate(EntityDropItemEvent e) {
		if (!(e.getEntity() instanceof Player p)) return;
		var user = um.getUser(p.getUniqueId());
		var loc = p.getLocation();
		var chunk = loc.getChunk();
		if (user == null) {
			var options = ClickCallback.Options.builder()
					.uses(1)
					.lifetime(java.time.Duration.ofMinutes(3))
					.build();

			Component reload = ColorUtils.chat(" &c&l[ 다시 로드하기 ]")
					.hoverEvent(HoverEvent.showText(ColorUtils.chat("&c&l클릭하여 유저 정보를 다시 로드합니다.")))
					.clickEvent(ClickEvent.callback(audience -> {
						if (audience instanceof Player t) {
							um.onJoinAsync(t.getUniqueId(), t.getName(), true).whenComplete((reloadedUser, ex) -> Bukkit.getScheduler().runTask(plugin, () -> {
								if (ex != null || reloadedUser == null) {
									t.sendMessage(ColorUtils.chat(Alert.RED + " 다시 로드하는 데 실패했습니다. 관리자에게 문의하세요."));
								} else {
									plugin.infoMsg(InfoLevel.INFO, t, "유저정보가 성공적으로 로드되었습니다!");
									UserTagManager.createHoverTag(t, reloadedUser);
									UserTagManager.runTask();
								}
							}));
						}
					}, options));

			p.sendMessage(ColorUtils.chat(Alert.RED + " 유저 정보를 불러오는데 실패하였습니다!").append(reload));
			return;
		}
		if (rm.hasRegion(loc)) {
			e.setCancelled(true);
			plugin.infoMsg(InfoLevel.ERROR, p, "이미 누군가의 구역입니다!");
			return;
		}
		Region region = new Region(p, loc);
		rm.addRegion(p, region);
		plugin.infoMsg(InfoLevel.INFO, p, "구역을 생성하였습니다! (ID : &6" + region.Id() + "&f)");
	}
}
