package xmasLegacy.Region;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import xmasLegacy.Region.Events.RegionDeleteEvent;
import xmasLegacy.Region.Events.RegionGenerateEvent;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.io.File;
import java.io.IOException;
import java.util.*;

public enum RegionManager {
	INSTANCE;

	private final @NotNull XmasLegacy plugin;
	private final @NotNull Map<Long, Region> regions = new HashMap<>();
	private final @NotNull Map<UUID, List<Region>> userRegionsMap = new HashMap<>();
	private float globalAngle = 0.0f;
	private @NotNull File file;
	private @NotNull FileConfiguration config;

	RegionManager() {
		this.plugin = XmasLegacy.getInstance();
		setupFile();
		loadAll();
	}

	public void startGlobalIndicatorTask() {
		new BukkitRunnable() {
			int checkDelay = 0;

			@Override
			public void run() {
				if (regions.isEmpty()) return;

				checkDelay++;

				globalAngle += (float) Math.toRadians(3);
				if (globalAngle >= Math.PI * 2) globalAngle = 0.0f;
				Quaternionf leftRotation = new Quaternionf(new AxisAngle4f(globalAngle, 0.0f, 1.0f, 0.0f));

				for (Region region : regions.values()) {
					if (region.getIndicator() == null || !region.getIndicator().isValid()) {
						if (region.getIndicatorUid() != null && checkDelay >= 20) {
							Entity entity = Bukkit.getEntity(region.getIndicatorUid());

							if (entity instanceof BlockDisplay bd) {
								region.setIndicator(bd);
							} else {
								var chunk = region.getChunk();
								if (chunk != null && chunk.isLoaded()) {
									Bukkit.getScheduler().runTask(plugin, () -> {
										region.setIndicator(indicatorDisplay(region));
										saveAll();
										plugin.getSLF4JLogger().warn("[Region] 구역 {}의 인디케이터가 유실되어 자동 재생성되었습니다.", region.Id());
									});
								}
							}
						}
					}
					setTrans(region, leftRotation);
				}
				if (checkDelay >= 20) checkDelay = 0;
			}
		}.runTaskTimer(plugin, 0L, 1L);
	}

	private void setTrans(@NotNull Region region, @NotNull Quaternionf leftRotation) {
		if (region.getIndicator() != null && region.getIndicator().isValid()) {
			Transformation transformation = region.getIndicator().getTransformation();
			Transformation newTrans = new Transformation(
					transformation.getTranslation(),
					leftRotation,
					transformation.getScale(),
					transformation.getRightRotation()
			);
			region.getIndicator().setTransformation(newTrans);
		}
	}

	/**
	 * Region Indicator must be Registered.
	 * @param region region
	 * @return BlockDisplay
	 */
	@CheckReturnValue
	public @Nullable BlockDisplay indicatorDisplay(@NotNull Region region) {
		var chunk = region.getChunk();
		return chunk == null ? null : region.getWorld().spawn(region.getTrueCenter(chunk).clone().add(-0.25, 0.5, -0.25), BlockDisplay.class, b -> {
			b.setBlock(Material.BEACON.createBlockData());
			b.setGravity(false);
			b.setGlowColorOverride(Color.AQUA);
			b.customName(ColorUtils.chat("&b구역 : " + region.Id()));
			b.setCustomNameVisible(true);
			Transformation trans = b.getTransformation();
			trans.getScale().set(0.5f);
			trans.getTranslation().set(-0.25f, 0.0f, -0.25f);
			b.setTransformation(trans);
			b.getPersistentDataContainer().set(plugin.getNamespacedKey(Constants.regionKey), PersistentDataType.STRING, "indicator");
		});
	}

	public static @NotNull ItemStack RegionTicket() {
		var plugin = XmasLegacy.getInstance();
		return ItemBuilder.of(plugin, Material.FIELD_MASONED_BANNER_PATTERN)
				.hideAllFlags()
				.setName(ColorUtils.chat("&6&l구역 티켓"))
				.setLore(ColorUtils.chat("&7아이템을 던져 해당 청크를 구매하세요!"))
				.setGlint(true)
				.setTag("region", "beacon")
				.setMaxStackSize(16)
				.build().clone();
	}

	public boolean isTicket(@NotNull ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) return false;
		PersistentDataContainer container = meta.getPersistentDataContainer();
		String value = container.get(plugin.getNamespacedKey("region"), PersistentDataType.STRING);
		return value != null && value.equals("beacon");
	}

	private void setupFile() {
		if (!plugin.getDataFolder().exists()) {
			if (plugin.getDataFolder().mkdir()) {
				plugin.getLogger().info("[RegionManager] Data folder created.");
			} else {
				plugin.getSLF4JLogger().error("[RegionManager] Failed to create data folder at: {}", plugin.getDataFolder().getAbsolutePath());
			}
		}
		file = new File(plugin.getDataFolder(), "regions.yml");
		if (!file.exists()) {
			try {
				if (file.createNewFile()) {
					plugin.getLogger().info("[RegionManager] regions.yml file created.");
				} else {
					plugin.getSLF4JLogger().error("[RegionManager] Failed to create regions.yml file.");
				}
			} catch (IOException e) {
				plugin.getSLF4JLogger().error("파일 생성 중 문제 발생: {}", e.getMessage(), e);
			}
		}
		config = YamlConfiguration.loadConfiguration(file);
	}

	public void saveAll() {
		config.set("regions", null);

		for (Region region : regions.values()) {
			String path = "regions." + region.Id();
			config.set(path + ".owner", region.getOwner().toString());
			config.set(path + ".id", region.Id());
			config.set(path + ".world", region.getWorld().getName());
			config.set(path + ".key", region.key());
			config.set(path + ".allowEntry", region.isEntryAllowed());
			config.set(path + ".allowInteract", region.isInteractionAllowed());
			if (region.getIndicatorUid() != null) {
				config.set(path + ".indicatorUuid", region.getIndicatorUid().toString());
			}
		}

		try {
			config.save(file);
		} catch (IOException e) {
			plugin.getSLF4JLogger().error("구역 데이터를 저장하는 중 오류 발생: {}", e.getMessage(), e);
		}
	}

	public void loadAll() {
		if (!config.contains("regions") || config.getConfigurationSection("regions") == null) return;

		regions.clear();
		userRegionsMap.clear();

		for (String configKey : Objects.requireNonNull(config.getConfigurationSection("regions")).getKeys(false)) {
			String path = "regions." + configKey;

			String ownerStr = config.getString(path + ".owner");
			if (ownerStr == null) continue;

			UUID owner = UUID.fromString(ownerStr);
			String id = config.getString(path + ".id");
			long chunkKey = config.getLong(path + ".key");
			String wk = config.getString(path + ".world");
			World world = Bukkit.getWorld(wk != null ? wk : "world");
			if (world == null) continue;

			boolean entry = config.getBoolean(path + ".allowEntry");
			boolean interact = config.getBoolean(path + ".allowInteract");

			Region region = new Region(owner, id != null ? id : "null", world, chunkKey, entry, interact);
			String uuidStr = config.getString(path + ".indicatorUuid");
			if (uuidStr != null) {
				region.setIndicator(UUID.fromString(uuidStr));
			}
			regions.put(chunkKey, region);
			userRegionsMap.computeIfAbsent(owner, k -> new ArrayList<>()).add(region);
		}
		plugin.getSLF4JLogger().info("총 {}개의 구역 데이터를 성공적으로 로드했습니다.", regions.size());
	}

	public void addRegion(Player p, Region region) {
		if (region == null || !region.isValid()) return;
		var event = new RegionGenerateEvent(p, region, region.getOwner(), region.Id());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		regions.put(region.key(), region);
		userRegionsMap.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>()).add(region);
		saveAll();
	}

	public void removeRegion(Region region) {
		if (region == null) return;
		UUID ownerUUID = region.getOwner();

		var event = new RegionDeleteEvent(ownerUUID, region, region.Id());
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) return;

		if (region.getIndicator() != null && region.getIndicator().isValid()) {
			region.getIndicator().remove();
		}

		regions.remove(region.key());


		List<Region> userRegions = userRegionsMap.get(ownerUUID);
		if (userRegions != null) {
			if (userRegions.remove(region)) {
				if (userRegions.isEmpty()) {
					userRegionsMap.remove(ownerUUID);
				}
				saveAll();
				plugin.getSLF4JLogger().info("구역이 삭제되었습니다. ID: {}", region.Id());
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
				regions.remove(region.key());
			});
			saveAll();
		}
	}

	public @NotNull List<Region> getRegion(@NotNull Player p) {
		return userRegionsMap.getOrDefault(p.getUniqueId(), new ArrayList<>());
	}

	public @NotNull List<Region> getRegion(@NotNull UUID uuid) {
		return userRegionsMap.getOrDefault(uuid, new ArrayList<>());
	}

	public @Nullable Region getRegionAt(Location loc) {
		if (loc == null) return null;
		return regions.get(loc.getChunk().getChunkKey());
	}

	public @Nullable Region getRegion(String id) {
		if (id == null) return null;
		return regions.values().stream()
				.filter(region -> region.Id().equals(id))
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

	@NotNull
	public List<Region> getRegions() {
		return new ArrayList<>(regions.values());
	}
}