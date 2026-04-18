package org.lazberry.xmasLegacy.Region;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmasLegacy.UserManager;
import org.lazberry.xmasLegacy.XmasLegacy;

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
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(file);
    }

    // 데이터 저장 로직
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
            plugin.getLogger().severe("구역 데이터를 저장하는 중 오류 발생!");
            e.printStackTrace();
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

            // 메모리에 적재
            regions.computeIfAbsent(owner, k -> new ArrayList<>()).add(region);
        }
        plugin.getLogger().info("[Region] " + regions.size() + "명의 유저 구역 데이터를 로드했습니다.");
    }

    public void addRegion(Player p, Region region) {
        regions.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>()).add(region);
        saveAll();
    }
    public void removeAllRegion(Player p) {
        regions.remove(p.getUniqueId());
        saveAll();
    }
    public @Nullable List<Region> getRegion(Player p) {
        return regions.getOrDefault(p.getUniqueId(), new ArrayList<>());
    }
    public @Nullable List<Region> getRegion(UUID uuid) {
        return regions.getOrDefault(uuid, new ArrayList<>());
    }
    public @Nullable Region getRegionAt(Location loc) {
        for (List<Region> list : regions.values()) {
            for (Region region : list) {
                if (region.isInsideOuterZone(loc)) return region;
            }
        }
        return null;
    }
}
