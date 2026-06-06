package xmasLegacy.Region;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.InfoLevel;
import xmasLegacy.ServerPrefix.UserTagManager;
import xmasLegacy.XmasLegacy;

import java.util.Arrays;

@SuppressWarnings("DuplicatedCode")
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
		p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
		p.openInventory(new RegionCreateInterface().getInventory());
	}

	@EventHandler
	public void MakeRegionIndicator(RegionGenerateEvent e) {
		var region = e.getRegion();

		Chunk chunk = region.getChunk();
		if (!region.isValid()) return;
		if (chunk == null) return;

		Location center = region.getTrueCenter(chunk);
		BlockDisplay indic = region.getWorld().spawn(center.clone().add(0, 0.5, 0), BlockDisplay.class, b -> {
			b.setBlock(Material.BEACON.createBlockData());
			b.setGravity(false);
			b.setGlowColorOverride(Color.AQUA);
			b.customName(ColorUtils.chat("&b&l구역 : " + region.Id()));
			b.setCustomNameVisible(true);
			Transformation trans = b.getTransformation();
			trans.getScale().set(0.5f);
			b.getPersistentDataContainer().set(plugin.getNamespacedKey(Constants.regionKey), PersistentDataType.STRING, "indicator");
		});
	}

	@EventHandler
	public void removeWhenDeleted(RegionDeleteEvent e) {
		var chunk = e.getRegion().getChunk();
		if (chunk == null) return;

		Arrays.stream(chunk.getEntities())
				.filter(entity -> entity instanceof BlockDisplay)
				.filter(entity -> entity.getPersistentDataContainer().has(plugin.getNamespacedKey(Constants.regionKey), PersistentDataType.STRING))
				.forEach(Entity::remove);
	}
}
