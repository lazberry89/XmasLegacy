package org.lazberry.xmaslegacy.region;

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
import org.jetbrains.annotations.Unmodifiable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.region.event.RegionDeleteEvent;
import org.lazberry.xmaslegacy.region.event.RegionGenerateEvent;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.*;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class RegionManager implements Initiator {
	private final Map<Long, Region> regions = new HashMap<>();
	private final Map<UUID, List<Region>> userRegionsMap = new HashMap<>();
	private final RegionConfig config;

	@Inject
	public RegionManager(RegionConfig config) {
		this.config = config;
	}

	@Override
	public void init() {
		regions.clear();
		userRegionsMap.putAll(config.loadSync());
		userRegionsMap.values().forEach(l ->
			l.forEach(r -> regions.put(r.getKey(), r)));
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

	public void addRegion(Player p, Region region) {
		if (region == null || !region.isValid()) return;
		var event = new RegionGenerateEvent(p, region, region.getOwner(), region.getId());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		regions.put(region.getKey(), region);
		userRegionsMap.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>()).add(region);
		config.saveAsync(userRegionsMap);
	}

	public void removeRegion(Region region) {
		if (region == null) return;
		UUID ownerUUID = region.getOwner();

		var event = new RegionDeleteEvent(ownerUUID, region, region.getId());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		if (region.getIndicator() != null && region.getIndicator().isValid()) {
			region.getIndicator().remove();
		}

		regions.remove(region.getKey());


		List<Region> userRegions = userRegionsMap.get(ownerUUID);
		if (userRegions != null) {
			if (userRegions.remove(region)) {
				if (userRegions.isEmpty()) {
					userRegionsMap.remove(ownerUUID);
				}
				config.saveAsync(userRegionsMap);
				log.info("구역이 삭제되었습니다. ID: {}", region.getId());
			}
		}
	}

	public void removeAllRegion(Player p) {
		List<Region> userRegions = userRegionsMap.remove(p.getUniqueId());
		if (userRegions != null) {
			userRegions.forEach(region -> {
				if (region.getIndicator() != null && region.getIndicator().isValid()) {
					region.getIndicator().remove();
				}
				regions.remove(region.getKey());
			});
			config.saveAsync(userRegionsMap);
		}
	}

	public List<Region> getRegion(Player p) {
		return getRegion(p.getUniqueId());
	}

	public List<Region> getRegion(UUID uuid) {
		return userRegionsMap.getOrDefault(uuid, new ArrayList<>());
	}

	public @Nullable Region getRegionAt(Location loc) {
		if (loc == null) return null;
		return regions.get(loc.getChunk().getChunkKey());
	}

	public @Nullable Region getRegion(String id) {
		if (id == null) return null;
		return regions.values().stream()
				.filter(region -> region.getId().equals(id))
				.findFirst()
				.orElse(null);
	}

	public boolean hasRegion(long chunkKey) {
		return regions.containsKey(chunkKey);
	}

	public boolean hasRegion(@Nullable Chunk chunk) {
		if (chunk == null) return false;
		return regions.containsKey(chunk.getChunkKey());
	}

	public boolean hasRegion(@Nullable Location loc) {
		if (loc == null) return false;
		return regions.containsKey(loc.getChunk().getChunkKey());
	}

	public void sendRegionFormat(@NotNull Player p, @NotNull List<Region> regions) {
		for (Region region : regions) {
			p.sendMessage(ColorUtils.chat("&8&l--------------------------------"));
			p.sendMessage(ColorUtils.chat("&6&lRegion ID : &f" + region.getId()));
			p.sendMessage(ColorUtils.chat("&eOwner : &f" + region.getOwner()));

			int x = region.getChunkX();
			int z = region.getChunkZ();
			String world = region.getWorld().getName();

			p.sendMessage(ColorUtils.chat(String.format("&eLocation : &7%s (%d, %d)", world, x, z)));

			String entry = region.isEntryAllowed() ? "&a허용" : "&c차단";
			String interact = region.isInteractionAllowed() ? "&a허용" : "&c차단";
			p.sendMessage(ColorUtils.chat(String.format("&eSettings : &f출입[%s&f] 상호작용[%s&f]", entry, interact)));
		}
		p.sendMessage(ColorUtils.chat("&8&l--------------------------------"));
	}

	@NotNull
	public List<Region> getRegions() {
		return new ArrayList<>(regions.values());
	}

	@Unmodifiable
	public Map<Long, Region> getRegionMap() {
		return new HashMap<>(this.regions);
	}
}