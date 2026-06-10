package org.lazberry.xmaslegacy.Inquiry;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.RuleManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InquiryManager {
	private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
	private final Map<UUID, String> activeInquiries = new ConcurrentHashMap<>();
	private final int cooldownTime = Constants.INQUIRY_COOLDOWN;
	private final UserManager um;
	private final RuleManager rm;
	private final InquiryRepository repository;

	private static volatile InquiryManager instance;

	private InquiryManager() {
		this.um = UserManager.getInstance();
		this.rm = RuleManager.getInstance();
		this.repository = new InquiryRepository();
	}

	public static InquiryManager getInstance() {
		if (instance == null) {
			synchronized (InquiryManager.class) {
				if (instance == null) instance = new InquiryManager();
			}
		}
		return instance;
	}

	public Component Inquiry(UUID uuid, @Nullable String message) {
		User user = um.getUser(uuid);
		String name = (user != null) ? user.getName() : "Unknown";
		if (message == null) {
			activeInquiries.put(uuid, "내용 없는 호출");
			return ColorUtils.chat("이동하시겠습니까? ")
					.append(ColorUtils.chat("&a&l[이동]"))
					.clickEvent(ClickEvent.runCommand("/이동문의 " + name))
					.hoverEvent(HoverEvent.showText(ColorUtils.chat("클릭 시 문의한 유저의 위치로 이동합니다.")));
		} else {
			activeInquiries.put(uuid, message);
			Component msg = ColorUtils.chat("이동하시겠습니까? ");
			Component nt = ColorUtils.chat("&a&l[이동]")
					.clickEvent(ClickEvent.runCommand("/이동문의 " + name))
					.hoverEvent(HoverEvent.showText(ColorUtils.chat(Alert.YELLOW + " 클릭 시 문의한 유저의 위치로 이동합니다.")));
			msg = msg.append(nt);
			if (rm.checkBadWords(message)) {
				Component bwt = ColorUtils.chat(" &c&l[처벌]")
						.clickEvent(ClickEvent.runCommand("/ban " + name + " 욕설사용"))
						.hoverEvent(HoverEvent.showText(ColorUtils.chat(Alert.RED + " 클릭 시 유저를 밴 처리합니다.")));
				msg = msg.append(bwt);
			}
			return msg;
		}
	}

	public int getCooldownTime() {
		return cooldownTime;
	}

	public void updateInquiryStatus(UUID uuid, InquiryStatus status) {
		repository.updateStatus(uuid, status.name());
	}

	// 대기 목록에서 삭제
	public void removeInquiry(UUID uuid) {
		activeInquiries.remove(uuid);
	}

	public boolean hasInquiry(UUID uuid) {
		return activeInquiries.containsKey(uuid);
	}

	public Map<UUID, String> getInquiryMap() {
		return activeInquiries;
	}

	public List<String> getInquiryLogs(UUID uuid) {
		List<String> logs = repository.getLogs(uuid);
		return logs.isEmpty() ? List.of("&c기록이 없습니다.") : logs;
	}

	public boolean checkAndSetCooldown(UUID uuid) {
		long now = System.currentTimeMillis();
		if (cooldowns.containsKey(uuid)) {
			long lastTime = cooldowns.get(uuid);
			long secondsPassed = (now - lastTime) / 1000;

			if (secondsPassed < Constants.INQUIRY_COOLDOWN) {
				return true; // 아직 쿨타임 중
			}
		}

		cooldowns.put(uuid, now);
		return false;
	}

	public long getRemainingCooldown(UUID uuid) {
		if (!cooldowns.containsKey(uuid)) return 0;
		long secondsPassed = (System.currentTimeMillis() - cooldowns.get(uuid)) / 1000;
		return Math.max(0, Constants.INQUIRY_COOLDOWN - secondsPassed);
	}
}

