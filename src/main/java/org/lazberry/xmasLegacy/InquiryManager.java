package org.lazberry.xmasLegacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ComponentChanger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class InquiryManager {
	private final Map<UUID, Long> cooldowns = new HashMap<>();
	private Map<UUID, String> inquiryMessage = new HashMap<>();
    private final XmasLegacy plugin;
	private final int cooldownTime = 30;
	private final RuleManager rm;

	public InquiryManager(RuleManager rm, XmasLegacy plugin) {
		this.rm = rm;
        this.plugin = plugin;
	}

	public List<Player> getAvailableOperator() {
		List<Player> ao = new ArrayList<>();

		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.isOp() && p.isOnline()) { //방해금지 설정 추후 추가
				ao.add(p);
			}
		}
		return ao;
	}

	public boolean isCooldown(Player player) {
		UUID uuid = player.getUniqueId();
		long currentTime = System.currentTimeMillis();

		if (cooldowns.containsKey(uuid)) {
			long lastUsed = cooldowns.get(uuid);
			long secondsLeft = ((lastUsed + (cooldownTime * 1000L)) - currentTime) / 1000;

			if (secondsLeft > 0) {
				player.sendMessage(ColorUtils.chat("&c&l[!]&f 아직 문의를 보낼 수 없습니다. &d" + secondsLeft + "&f 초 후에 다시 시도하세요."));
				return true;
			}
		}

		cooldowns.put(uuid, currentTime);
		return false;
	}

	public void Inquiry(UUID uuid, @Nullable String message) {
		OfflinePlayer inqSender = Bukkit.getOfflinePlayer(uuid);

		if (message == null) {
			inquiryMessage.put(uuid, "내용 없는 호출");
            saveInquiry(uuid, "내용 없는 호출");
			for (Player ops : getAvailableOperator()) {
				Component msg = ComponentChanger.comp("이동하시겠습니까? ")
						.append(ComponentChanger.comp("&a&l[이동]"))
						.clickEvent(ClickEvent.runCommand("/이동문의 " + inqSender.getName()))
						.hoverEvent(HoverEvent.showText(ComponentChanger.comp("클릭 시 문의한 유저의 위치로 이동합니다.")));
				ops.sendMessage(ColorUtils.chat("&b&l[문의]&f " + inqSender.getName() + " 님이 보냄 : (비어있음)"));
				ops.sendMessage(msg);
			}
		} else {
			inquiryMessage.put(uuid, message);
            saveInquiry(uuid, message);
			if (rm.checkBadWords(message)) {
				if (inqSender.getPlayer() != null && inqSender.isOnline()) {
					inqSender.getPlayer().sendMessage(ColorUtils.chat(Prefix.RED + " 욕설이 포함된 문의는 &6운영자&f에 의해 &c제재&f를 받을 수 있습니다."));
				}
			}
			for (Player ops : getAvailableOperator()) {
				Component msg = ComponentChanger.comp("이동하시겠습니까? ");
                Component nt = ComponentChanger.comp("&a&l[이동]")
										.clickEvent(ClickEvent.runCommand("/이동문의 " + inqSender.getName()))
												.hoverEvent(HoverEvent.showText(ComponentChanger.comp(Prefix.YELLOW + " 클릭 시 문의한 유저의 위치로 이동합니다.")));
                Component bwt = ComponentChanger.comp(" &c&l[처벌]")
                        .clickEvent(ClickEvent.runCommand("/ban " +  inqSender.getName()))
                        .hoverEvent(HoverEvent.showText(ComponentChanger.comp(Prefix.RED + "클릭 시 유저를 밴 처리합니다.")));
                msg = msg.append(nt);
                if (rm.checkBadWords(message) && inqSender.isOnline() && !inqSender.isOp()) msg = msg.append(bwt);

				ops.sendMessage(ColorUtils.chat("&b&l[문의]&f " + inqSender.getName() + " 님이 보냄 : &7" + rm.hideBadWords(message)));
				ops.sendMessage(msg);
			}
		}
	}
    public void saveInquiry(UUID uuid, String message) {
        File logFile = new File(plugin.getDataFolder(), "inquiry_logs.yml");
        FileConfiguration logConfig = YamlConfiguration.loadConfiguration(logFile);

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String playerName = Bukkit.getOfflinePlayer(uuid).getName();

        String path = "logs." + uuid + "." + time.replace(":", "-");

        logConfig.set(path + ".player", playerName);
        logConfig.set(path + ".message", message);
        logConfig.set(path + ".status", InquiryStatus.OPEN.name());

        try {
            logConfig.save(logFile);
        } catch (IOException e) {
            plugin.getLogger().warning("문의 로그를 로드하지 못하였습니다.");
        }
    }
    public List<String> getInquiryLogs(UUID uuid) {
        List<String> history = new ArrayList<>();
        File logFile = new File(plugin.getDataFolder(), "inquiry_logs.yml");
        FileConfiguration logConfig = YamlConfiguration.loadConfiguration(logFile);

        ConfigurationSection userSection = logConfig.getConfigurationSection("logs." + uuid);

        if (userSection == null) {
            history.add(ColorUtils.chat("&c&l[!]&f 해당 유저의 문의 기록이 없습니다."));
            return history;
        }

        for (String timeKey : userSection.getKeys(false)) {
            String message = userSection.getString(timeKey + ".message");
            String status = userSection.getString(timeKey + ".status");

            String displayTime = timeKey.replace("-", ":");
            history.add(ColorUtils.chat("&b&l[문의]&7&l[" + displayTime + "] &f" + message + " &7(" + status + ")"));
        }

        return history;
    }
    public void updateInquiryStatus(UUID uuid, InquiryStatus newStatus) {
        File logFile = new File(plugin.getDataFolder(), "inquiry_logs.yml");
        FileConfiguration logConfig = YamlConfiguration.loadConfiguration(logFile);

        ConfigurationSection userSection = logConfig.getConfigurationSection("logs." + uuid);

        if (userSection == null) return;

        List<String> timeKeys = new ArrayList<>(userSection.getKeys(false));
        if (timeKeys.isEmpty()) return;

        String latestKey = timeKeys.getLast();

        logConfig.set("logs." + uuid + "." + latestKey + ".status", newStatus);

        try {
            logConfig.save(logFile);
        } catch (IOException e) {
            plugin.getLogger().warning("[!] 문의 상태 업데이트 중 오류 발생: " + uuid);
        }
    }
	public String getInquiry(UUID uuid) {
		return inquiryMessage.getOrDefault(uuid, ColorUtils.chat("&e&l[!]&f 아직 문의가 없습니다."));
	}
    public Map<UUID, String> getInquiryMap() {
        return this.inquiryMessage;
    }

	public boolean hasInquiry(UUID uuid) {
		return inquiryMessage.containsKey(uuid);
	}

	public void setInquiry(UUID uuid, String message) {
		inquiryMessage.put(uuid, message);
	}

	public void removeInquiry(UUID uuid) {
		inquiryMessage.remove(uuid);
	}

	public Long getCooldowns(UUID uuid) {
		return cooldowns.getOrDefault(uuid, 0L);
	}
}
