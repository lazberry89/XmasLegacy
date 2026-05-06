package xmasLegacy.Region;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.User.UserManager;
import xmasLegacy.XmasLegacy;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RegionManager {
    private final XmasLegacy plugin;
    private final UserManager UM;
    private final Map<UUID, List<Region>> regions = new HashMap<>();
    private File file;
    private FileConfiguration config;

    public RegionManager(XmasLegacy plugin, UserManager UM) {
        this.plugin = plugin;
        this.UM = UM;
        setupFile();
        loadAll();
    }

    private void setupFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }
        file = new File(plugin.getDataFolder(), "regions.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getSLF4JLogger().error("파일생성중 문제: {}", e.getMessage(), e);
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    public void saveAll() {
        config.set("regions", null); // 초기화

        for (List<Region> list : regions.values()) {
            for (Region region : list) {
                String path = "regions." + region.getId(); // ID 기반 저장
                config.set(path + ".owner", region.getOwner().toString()); // 주인 정보 저장
                config.set(path + ".id", region.getId());
                config.set(path + ".center", region.getCenter());
                config.set(path + ".allowEntry", region.isAllowPublicEntry());
                config.set(path + ".allowInteract", region.isAllowPublicInteraction());
            }
        }

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getSLF4JLogger().error("구역 데이터를 저장하는 중 오류: {}", e.getMessage(), e);
        }
    }

    // 데이터 로드 로직
    public void loadAll() {
        if (!config.contains("regions") || config.getConfigurationSection("regions") == null) return;

        regions.clear();

        for (String key : config.getConfigurationSection("regions").getKeys(false)) {
            String path = "regions." + key;

            // 저장된 값 읽기
            String ownerStr = config.getString(path + ".owner");
            if (ownerStr == null) continue; // 데이터 오염 방지

            UUID owner = UUID.fromString(ownerStr);
            String id = config.getString(path + ".id");
            Location center = config.getLocation(path + ".center");
            boolean entry = config.getBoolean(path + ".allowEntry");
            boolean interact = config.getBoolean(path + ".allowInteract");

            Region region = new Region(owner, id, center, entry, interact, UM);

            regions.computeIfAbsent(owner, k -> new ArrayList<>()).add(region);
        }
        plugin.getSLF4JLogger().info("[Region] {}명의 유저 구역 데이터를 로드했습니다.", regions.size());
    }

    public void addRegion(Player p, Region region) {
        regions.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>()).add(region);
        saveAll();
    }
	public void removeRegion(Region region) {
		if (region == null) return;

		UUID ownerUUID = region.getOwner();
		if (regions.containsKey(ownerUUID)) {
			List<Region> userRegions = regions.get(ownerUUID);

			if (userRegions.remove(region)) {
				if (userRegions.isEmpty()) {
					regions.remove(ownerUUID);
				}
				saveAll();
                plugin.getSLF4JLogger().info("[Region] 구역이 삭제되었습니다. ID: {}", region.getId());
			}
		}
	}
    public void removeAllRegion(Player p) {
        regions.remove(p.getUniqueId());
        saveAll();
    }
    public @Nullable List<Region> getRegion(Player p) {
        return regions.getOrDefault(p.getUniqueId(), new ArrayList<>());
    }
    public @Nullable List<Region> getRegion(UUID uuid) {
        return regions.getOrDefault(uuid, null);
    }
    public @Nullable Region getRegionAt(Location loc) {
        for (List<Region> list : regions.values()) {
            for (Region region : list) {
                if (region.isInsideOuterZone(loc)) return region;
            }
        }
        return null;
    }
	public @Nullable Region getRegion(String id) {
		for (List<Region> list : regions.values()) {
			for (Region region : list) {
				if (region.getId().equals(id)) return region;
			}
		}
		return null;
	}
	public boolean isOverlaps(Region r1, Region r2) {
		if (r1 == null || r2 == null) return false;
		return r1.overlaps(r2);
	}
	@NotNull
	public List<Region> getRegions() {
		return regions.values().stream().flatMap(List::stream).toList();
	}
}
