package org.lazberry.xmasLegacy.User;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmasLegacy.Roles.Roles;
import org.lazberry.xmasLegacy.XmasLegacy;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private final Map<UUID, User> users = new ConcurrentHashMap<>();
    private final XmasLegacy plugin;

    public UserManager(@NotNull XmasLegacy plugin) {
        this.plugin = plugin;
    }

    public void loadUser(Player p) {
        User user = new User(p, null); // 일단 기본 틀 생성
        File userFile = new File(plugin.getDataFolder() + "/users", p.getUniqueId() + ".yml");

        if (userFile.exists()) {
            // [기존 유저] 파일이 있으면 덮어씌우기
            FileConfiguration config = YamlConfiguration.loadConfiguration(userFile);

            user.setDollars(config.getInt("dollars", user.getDollars())); // 파일에 없으면 기본값 유지
            user.setInquireCount(config.getInt("inquireCount", 0));
            user.setPlayTime(config.getInt("playTime", 0));

            String roleStr = config.getString("role");
            if (roleStr != null) {
                try {
                    user.setRole(Roles.valueOf(roleStr)); // 문자열을 다시 Enum으로 변환
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning(p.getName() + "의 역할 데이터를 불러오지 못했습니다.");
                }
            }
        }
        // 파일이 없다면 신규 유저이므로 User 클래스 생성자에 있는 초기자금(1000/5000)이 그대로 유지됨.

        // 맵에 최종적으로 저장
        users.put(p.getUniqueId(), user);
    }

    // 2. 유저 저장하고 메모리에서 지우기 (퇴장 시)
    public void saveAndRemoveUser(Player p) {
        User user = users.get(p.getUniqueId());
        if (user == null) return; // 맵에 없으면 무시

        saveUserToFile(user); // 파일에 저장
        users.remove(p.getUniqueId()); // ⭐ 메모리 누수 방지!
    }

    // 3. 실제 파일 저장 로직 (자동 저장 기능 등을 위해 분리)
    public void saveUserToFile(User user) {
        File userDir = new File(plugin.getDataFolder(), "users");
        if (!userDir.exists()) userDir.mkdirs(); // 폴더가 없으면 생성

        File userFile = new File(userDir, user.getUUID() + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(userFile);

        // 객체의 값들을 config에 쓰기
        config.set("name", user.getName());
        config.set("dollars", user.getDollars());
        config.set("inquireCount", user.getInquireCount());
        config.set("playTime", user.getPlayTime());
        if (user.getRole() != null) {
            config.set("role", user.getRole().name()); // Enum을 문자열로 저장
        }

        // 실제 파일로 디스크에 쓰기
        try {
            config.save(userFile);
        } catch (IOException e) {
            plugin.getLogger().severe("유저 데이터 저장 실패: " + user.getName());
        }
    }

    public void addUser(Player p, User user) {
        users.put(p.getUniqueId(), user);
    }
    public void removeUser(Player p) {
        users.remove(p.getUniqueId());
    }
    public User getUser(Player p) {
	    if (p == null) return null;
	    return users.get(p.getUniqueId());
    }
    public List<User> getAllUsers() {
        List<User> result = new ArrayList<>();
        for (UUID uuid : users.keySet()) {
            User user = users.get(uuid);
            result.add(user);
        }
        return result;
    }
    public boolean withdraw(Player p, int amount) {
        User user = getUser(p);
        if (user != null && user.getDollars() >= amount) {
            user.setDollars(user.getDollars() - amount);
            return true;
        }
        return false;
    }

    public void deposit(Player p, int amount) {
        User user = getUser(p);
        if (user != null) {
            user.setDollars(user.getDollars() + amount);
        }
    }
	public Roles getRoleByUUID(UUID uuid) {
		if (users.containsKey(uuid)) {
			return users.get(uuid).getRole();
		}

		File userFile = new File(plugin.getDataFolder() + "/users", uuid + ".yml");
		if (userFile.exists()) {
			FileConfiguration config = YamlConfiguration.loadConfiguration(userFile);
			String roleStr = config.getString("role");
			if (roleStr != null) {
				try {
					return Roles.valueOf(roleStr);
				} catch (IllegalArgumentException e) {
                    plugin.getServer().getLogger().warning("Exception: " + e.getMessage());
                    plugin.playConsoleSound();
                    return null;
				}
			}
		}
		return null;
	}
	public User getUser(UUID uuid) {
		return users.get(uuid);
	}
}
