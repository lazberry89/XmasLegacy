package org.lazberry.xmasLegacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ComponentChanger;

import java.util.*;

public class InquiryManager {
	private final Map<UUID, Long> cooldowns = new HashMap<>();
	private Map<UUID, String> inquiryMessage = new HashMap<>();
	private final int cooldownTime = 30;
	private final RuleManager rm;

	public InquiryManager(RuleManager rm) {
		this.rm = rm;
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
			if (rm.checkBadWords(message)) {
				if (inqSender.getPlayer() != null && inqSender.isOnline()) {
					inqSender.getPlayer().sendMessage(ColorUtils.chat(Prefix.RED + " 욕설이 포함된 문의는 &6운영자&f에 의해 &c제재&f를 받을 수 있습니다."));
				}
			}
			for (Player ops : getAvailableOperator()) {
				Component msg = ComponentChanger.comp("이동하시겠습니까? ")
								.append(ComponentChanger.comp("&a&l[이동]"))
										.clickEvent(ClickEvent.runCommand("/이동문의 " + inqSender.getName()))
												.hoverEvent(HoverEvent.showText(ComponentChanger.comp("클릭 시 문의한 유저의 위치로 이동합니다.")));
				ops.sendMessage(ColorUtils.chat("&b&l[문의]&f " + inqSender.getName() + " 님이 보냄 : &7" + rm.hideBadWords(message)));
				ops.sendMessage(msg);
			}
		}
	}
	public String getInquiry(UUID uuid) {
		return inquiryMessage.getOrDefault(uuid, ColorUtils.chat("&e&l[!]&f 아직 문의가 없습니다."));
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
