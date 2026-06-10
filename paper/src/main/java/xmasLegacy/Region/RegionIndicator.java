package xmasLegacy.Region;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.InfoLevel;
import xmasLegacy.ServerPrefix.UserTagManager;
import xmasLegacy.ServerTransfer;
import xmasLegacy.XmasLegacy;

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

		ItemStack item = e.getItemDrop().getItemStack();
		if (!rm.isTicket(item)) return;

		var user = um.getUser(p.getUniqueId());
		var loc = p.getLocation();
		if (user == null) ServerTransfer.loadUser(p);
		if (rm.hasRegion(loc)) {
			e.setCancelled(true);
			plugin.infoMsg(InfoLevel.ERROR, p, "이미 누군가의 구역입니다!");
			return;
		}
		p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
		p.openInventory(new RegionCreateInterface().getInventory());
		e.getItemDrop().remove();
	}

	@EventHandler
	public void MakeRegionIndicator(RegionGenerateEvent e) {
		var region = e.getRegion();

		Chunk chunk = region.getChunk();
		if (!region.isValid()) return;
		if (chunk == null) return;

		Location center = region.getTrueCenter(chunk);
		Location spawnLoc = center.clone().add(-0.25, 0.5, -0.25);


		BlockDisplay indic = region.getWorld().spawn(spawnLoc, BlockDisplay.class, b -> {
			b.setBlock(Material.BEACON.createBlockData());
			b.setGravity(false);
			b.setGlowColorOverride(Color.AQUA);
			b.customName(ColorUtils.chat("&b&l구역 : " + region.Id()));
			b.setCustomNameVisible(true);
			Transformation trans = b.getTransformation();
			trans.getScale().set(0.5f);
			b.setTransformation(trans);
			b.getPersistentDataContainer().set(plugin.getNamespacedKey(Constants.regionKey), PersistentDataType.STRING, "indicator");
		});
		if (indic.isValid()) region.setIndicator(indic);
	}

	@EventHandler
	public void removeWhenDeleted(RegionDeleteEvent e) {
		var chunk = e.getRegion().getChunk();
		if (chunk == null) return;

		var region = e.getRegion();
		var indic = region.getIndicator();
		if (indic != null && indic.isValid()) indic.remove();
	}

	@EventHandler
	public void RegionIndicatorClick(PlayerInteractAtEntityEvent e) {
		Player p = e.getPlayer();
		Entity target = e.getRightClicked();

		if (!target.getPersistentDataContainer().has(plugin.getNamespacedKey(Constants.regionKey))) return;
		p.playSound(p, Sound.BLOCK_BEACON_ACTIVATE, 0.5f, 1.0f);
		p.openInventory();
	}
}
