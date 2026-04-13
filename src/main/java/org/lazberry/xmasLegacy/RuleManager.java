package org.lazberry.xmasLegacy;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.lazberry.xmasLegacy.Utils.ColorUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RuleManager {
	private List<String> bad;
    private final XmasLegacy plugin;
    private final File file;
    private final FileConfiguration config;

    public RuleManager(XmasLegacy plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "filters.yml");
        if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();

        this.config = YamlConfiguration.loadConfiguration(file);
        this.bad = config.getStringList("words");

        if (bad.isEmpty()) {
            bad = new ArrayList<>(List.of("ㅅㅂ", "ㅄ", "시발", "장애", "지랄", "ㅈㄹ", "병신")); // 초기 데이터
            save();
        }
    }
    public void save() {
        try {
            config.set("words", bad);
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("[!] Filter 로드중 오류발생. 필터기능이 작동하지 않게됩니다.");
        }
    }

	public boolean checkBadWords(String s) {
		return bad.stream().anyMatch(s::contains);
	}

	public String hideBadWords(String message) {
		String processedMessage = message;

		for (String word : bad) {
			if (processedMessage.contains(word)) {
				processedMessage = processedMessage.replace(word, ColorUtils.chat("&k" + "#".repeat(word.length()) + "&r"));
			}
		}

		return processedMessage;
	}

    public void addBadWordList(String s) {
        this.bad.add(s);
        save();
    }
    public void removeBadWordList(String s) {
        this.bad.remove(s);
        save();
    }
    public List<String> getBadWordList() {
        return this.bad;
    }
}
