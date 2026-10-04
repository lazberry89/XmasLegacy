package org.lazberry.xmaslegacy.blueprint.preview;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.blueprint.BluePrint;
import org.lazberry.xmaslegacy.blueprint.BluePrintManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.Axiom;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BlueprintPreviewManager implements Initiator {
	private final XmasLegacy plugin;
	private final BluePrintManager bluePrintManager;
	private final Map<UUID, List<BlockDisplay>> activePreviews = new ConcurrentHashMap<>();
	private final Map<UUID, PreviewCache> lastCaches = new ConcurrentHashMap<>();
	private final Queue<PreviewExpireEntry> expireQueue = new ConcurrentLinkedQueue<>();
	private BukkitTask cleanupTask;

	@Inject
	public BlueprintPreviewManager(XmasLegacy plugin, BluePrintManager bluePrintManager) {
		this.plugin = plugin;
		this.bluePrintManager = bluePrintManager;
	}

	@Override
	public void init() {
		startCleanupTask();
	}

	@Override
	public void close() {
		clearAll();
	}

	private record PreviewCache(Location blockLoc, float yaw, String structureName, long createdAt) {}
	private record PreviewExpireEntry(UUID uuid, long createdAt, long expireTime) {}

	public void startCleanupTask() {
		if (cleanupTask != null && !cleanupTask.isCancelled()) return;
		this.cleanupTask = Bukkit.getScheduler().runTaskTimer(plugin, this::processExpiredPreviews, 5L, 5L);
	}

	public void updatePreview(Player player, long durationTicks) {
		ItemStack handItem = player.getInventory().getItemInMainHand();
		if (!BluePrint.isBluePrint(handItem)) {
			handItem = player.getInventory().getItemInOffHand();
			if (!BluePrint.isBluePrint(handItem)) {
				clearPreview(player.getUniqueId());
				return;
			}
		}

		String structureName = BluePrint.getNameByItem(handItem);
		var optionalBp = bluePrintManager.getBluePrint(structureName);
		if (optionalBp.isEmpty()) {
			clearPreview(player.getUniqueId());
			return;
		}

		Location currentBlockLoc = player.getLocation().getBlock().getLocation();
		float snappedYaw = Axiom.snapDegrees(player.getLocation().getYaw());
		long now = System.currentTimeMillis();

		PreviewCache lastCache = lastCaches.get(player.getUniqueId());
		if (lastCache != null
				&& lastCache.blockLoc().equals(currentBlockLoc)
				&& lastCache.yaw() == snappedYaw
				&& lastCache.structureName().equals(structureName)) {

			if (durationTicks > 0) {
				PreviewCache updatedCache = new PreviewCache(currentBlockLoc, snappedYaw, structureName, now);
				lastCaches.put(player.getUniqueId(), updatedCache);
				expireQueue.add(new PreviewExpireEntry(player.getUniqueId(), now, now + (durationTicks * 50L)));
			}
			return;
		}
		clearPreview(player.getUniqueId());

		PreviewCache newCache = new PreviewCache(currentBlockLoc, snappedYaw, structureName, now);
		lastCaches.put(player.getUniqueId(), newCache);

		if (durationTicks > 0) {
			expireQueue.add(new PreviewExpireEntry(player.getUniqueId(), now, now + (durationTicks * 50L)));
		}

		BluePrint blueprint = optionalBp.get();
		BluePrint rotatedBp = bluePrintManager.rotate(blueprint, snappedYaw);
		boolean canBuild = bluePrintManager.canBuild(rotatedBp, player, currentBlockLoc);

		Material displayMat = canBuild ? Material.LIME_STAINED_GLASS : Material.RED_STAINED_GLASS;
		var blockData = displayMat.createBlockData();

		List<BlockDisplay> displays = new ArrayList<>();

		rotatedBp.forEachVirtualLocation(currentBlockLoc, loc -> {
			Location spawnLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

			BlockDisplay display = loc.getWorld().spawn(spawnLoc, BlockDisplay.class, entity -> {
				entity.setBlock(blockData);
				entity.setBrightness(new Display.Brightness(15, 15));

				Transformation transformation = entity.getTransformation();
				transformation.getScale().set(new Vector3f(1.002f, 1.002f, 1.002f));
				transformation.getTranslation().set(new Vector3f(-0.001f, -0.001f, -0.001f));
				entity.setTransformation(transformation);
			});

			displays.add(display);
		});

		activePreviews.put(player.getUniqueId(), displays);
	}

	private void processExpiredPreviews() {
		long now = System.currentTimeMillis();

		while (!expireQueue.isEmpty()) {
			PreviewExpireEntry head = expireQueue.peek();
			if (head.expireTime() > now) {
				break;
			}

			expireQueue.poll();
			PreviewCache cache = lastCaches.get(head.uuid());
			if (cache != null && cache.createdAt() == head.createdAt()) {
				clearPreview(head.uuid());
			}
		}
	}

	public void clearPreview(UUID uuid) {
		lastCaches.remove(uuid);
		List<BlockDisplay> displays = activePreviews.remove(uuid);
		if (displays != null) {
			displays.forEach(Display::remove);
		}
	}

	public void clearAll() {
		if (cleanupTask != null) {
			cleanupTask.cancel();
			cleanupTask = null;
		}
		expireQueue.clear();
		activePreviews.keySet().forEach(this::clearPreview);
	}
}
