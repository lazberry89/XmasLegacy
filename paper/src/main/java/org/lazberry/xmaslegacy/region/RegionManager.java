package org.lazberry.xmaslegacy.region;

import org.bukkit.World;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.region.event.RegionDeleteEvent;
import org.lazberry.xmaslegacy.region.event.RegionGenerateEvent;
import org.lazberry.xmaslegacy.region.indicator.RegionDisplayManager;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.*;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class RegionManager implements Initiator {
	/** The same chunk coordinates exist in every world. */
	private record Position(UUID world, long chunk) {}

	private final Map<Position, Region> regions = new HashMap<>();
	private final Map<UUID, List<Region>> userRegionsMap = new HashMap<>();
	private final RegionDisplayManager displayManager;
	private final RegionConfig config;

	@Inject
	public RegionManager(RegionDisplayManager displayManager, RegionConfig config) {
        this.displayManager = displayManager;
        this.config = config;
	}

	@Override
	public void init() {
		restoreFromConfig(config.loadSync());
	}

	private void restoreFromConfig(Map<UUID, List<Region>> restored) {
		userRegionsMap.clear();
		regions.clear();
		userRegionsMap.putAll(restored);
		restored.values().forEach(r ->
			r.forEach(region -> {
				if (region != null && region.isValid()) {
					regions.put(position(region), region);
					if (displayManager.isLoaded(region)) displayManager.spawnIndicatorDisplay(region);
				}
			}));
		log.info("[Region] Restored {} regions from config.", regions.size());
	}

	public static ItemStack RegionTicket() {
		var plugin = XmasLegacy.getInstance();
		return ItemBuilder.of(plugin, Material.FIELD_MASONED_BANNER_PATTERN)
				.hideAllFlags()
				.name(ColorUtils.chat("&6&l구역 티켓"))
				.lore(ColorUtils.chat("&7아이템을 우클릭해 해당 청크를 구매하세요!"))
				.setGlint(true)
				.setTag("region", "beacon")
				.setMaxStackSize(16)
				.build();
	}

	public boolean isTicket(@Nullable ItemStack item) {
		if (item == null) return false;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return false;
		PersistentDataContainer container = meta.getPersistentDataContainer();
		String value = container.get(KeyUtils.get("region"), PersistentDataType.STRING);
		return value != null && value.equals("beacon");
	}

	/** @return false if it was refused. */
	public boolean addRegion(Player p, Region region) {
		if (region == null || !region.isValid()) return false;
		var event = new RegionGenerateEvent(p, region, region.getOwner(), region.getId());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return false;
		regions.put(position(region), region);
		userRegionsMap.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>()).add(region);
		config.saveSync(userRegionsMap);
		return true;
	}

	public void removeRegion(Region region) {
		if (region == null) return;
		UUID ownerUUID = region.getOwner();

		var event = new RegionDeleteEvent(ownerUUID, region, region.getId());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		// 인디케이터 디스플레이 및 인터랙션 엔티티 확실하게 제거
		displayManager.removeDisplay(region);
		regions.remove(position(region));
		List<Region> userRegions = userRegionsMap.get(ownerUUID);
		if (userRegions != null) {
			if (userRegions.remove(region)) {
				if (userRegions.isEmpty()) {
					userRegionsMap.remove(ownerUUID);
				}
				config.saveSync(userRegionsMap);
				log.info("구역이 삭제되었습니다. ID: {}", region.getId());
			}
		}
	}

	public void removeAllRegion(Player p) {
		List<Region> userRegions = userRegionsMap.remove(p.getUniqueId());
		if (userRegions != null) {
			userRegions.forEach(region -> {
				displayManager.removeDisplay(region);
				regions.remove(position(region));
				});
			config.saveSync(userRegionsMap);
		}
	}

	public @Nullable Region getRegion(World world, long key) {
		return regions.get(new Position(world.getUID(), key));
	}

	private static Position position(Region region) {
		return new Position(region.getWorld().getUID(), region.getKey());
	}

	/** From the coordinates: Location.getChunk() loads the chunk, and this runs on every move. */
	private static Position position(Location loc) {
		return new Position(loc.getWorld().getUID(), Chunk.getChunkKey(loc));
	}

	public void save() {
		config.saveSync(userRegionsMap);
	}

	public List<Region> getRegion(Player p) {
		return getRegion(p.getUniqueId());
	}

	public List<Region> getRegion(UUID uuid) {
		return userRegionsMap.getOrDefault(uuid, new ArrayList<>());
	}

	public @Nullable Region getRegionAt(Location loc) {
		if (loc == null || loc.getWorld() == null) return null;
		return regions.get(position(loc));
	}

	public @Nullable Region getRegion(String id) {
		if (id == null) return null;
		return regions.values().stream()
				.filter(region -> region.getId().equals(id))
				.findFirst()
				.orElse(null);
	}



	public boolean hasRegion(@Nullable Chunk chunk) {
		if (chunk == null) return false;
		return regions.containsKey(new Position(chunk.getWorld().getUID(), chunk.getChunkKey()));
	}

	public boolean hasRegion(@Nullable Location loc) {
		return getRegionAt(loc) != null;
	}

	public void sendRegionFormat(@NotNull Player p, @NotNull List<Region> regions) {
		for (Region region : regions) {
			p.sendMessage(ColorUtils.chat("&8&l--------------------------------"));
			p.sendMessage(ColorUtils.chat("&6&lRegion ID : &f" + region.getId()));
			p.sendMessage(ColorUtils.chat("&eOwner : &f" + region.getOwner()));

			int x = region.getChunkX();
			int z = region.getChunkZ();
			String world = region.getWorld() != null ? region.getWorld().getName() : "unknown";

			p.sendMessage(ColorUtils.chat(String.format("&eLocation : &7%s (%d, %d)", world, x, z)));

			String entry = region.isEntryAllowed() ? "&a허용" : "&c차단";
			String interact = region.isInteractionAllowed() ? "&a허용" : "&c차단";
			p.sendMessage(ColorUtils.chat(String.format("&eSettings : &f출입[%s&f] 상호작용[%s&f]", entry, interact)));
		}
		p.sendMessage(ColorUtils.chat("&8&l--------------------------------"));
	}

	@Override
	public void close() {
		cleanUp();
	}

	private void cleanUp() {
		// 1. 파일에 현재 구역 정보 동기 저장
		config.saveSync(userRegionsMap);

		// 2. 모든 인디케이터 디스플레이 및 인터랙션 엔티티 안전하게 제거
		regions.values().forEach(displayManager::removeDisplay);
		displayManager.clearDisplays();

		// 3. 맵 메모리 정리
		userRegionsMap.clear();
		regions.clear();
	}

	@NotNull
	public List<Region> getRegions() {
		return new ArrayList<>(regions.values());
	}


}
