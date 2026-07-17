package org.lazberry.xmaslegacy.Inquiry;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.RuleManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerManager;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Registry.Exclude(type = ServerType.LOBBY)
public class InquiryManager implements ServerManager {
	private final @NotNull Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
	private final @NotNull Map<UUID, String> activeInquiries = new ConcurrentHashMap<>();
    private final @Getter int cooldownTime = Constants.INQUIRY_COOLDOWN;
	private final @NotNull UserManager um;
	private final @NotNull RuleManager rm;
	private final @NotNull InquiryRepository repository;

	@Inject
	public InquiryManager(@NotNull UserManager um,  @NotNull RuleManager rm, @NotNull InquiryRepository repository) {
		this.um = um;
		this.rm = rm;
		this.repository = repository;
	}

	public @NotNull Map<UUID, String> getActiveInquiries() {
		return new HashMap<>(activeInquiries);
	}

	@Override
	public void init() {}

	public @NotNull Component Inquiry(@NotNull UUID uuid, @Nullable String message) {
		User user = um.getUser(uuid);
		String name = (user != null) ? user.getName() : "Unknown";

		String inquiryContent = (message != null) ? message : "내용 없는 호출";
		activeInquiries.put(uuid, inquiryContent);

		repository.saveInquiry(uuid, name, inquiryContent);

		Component msg = ColorUtils.chat("이동하시겠습니까? ");
		Component moveButton = ColorUtils.chat("&a&l[이동]")
				.clickEvent(ClickEvent.runCommand("/이동문의 " + name))
				.hoverEvent(HoverEvent.showText(ColorUtils.chat((message == null ? "" : Alert.YELLOW) + " 클릭 시 문의한 유저의 위치로 이동합니다.")));
		msg = msg.append(moveButton);

		if (message != null && rm.checkBadWords(message)) {
			Component punishButton = ColorUtils.chat(" &c&l[처벌]")
					.clickEvent(ClickEvent.runCommand("/ban " + name + " 욕설사용"))
					.hoverEvent(HoverEvent.showText(ColorUtils.chat(Alert.RED + " 클릭 시 유저를 밴 처리합니다.")));
			msg = msg.append(punishButton);
		}

		return msg;
	}

    public void updateInquiryStatus(@NotNull UUID uuid, @NotNull InquiryStatus status) {
		repository.updateStatus(uuid, status.name());
	}

	public void removeInquiry(@NotNull UUID uuid) {
		activeInquiries.remove(uuid);
	}

	public boolean hasInquiry(@NotNull UUID uuid) {
		return activeInquiries.containsKey(uuid);
	}

	public @NotNull List<String> getInquiryLogs(@NotNull UUID uuid) {
		List<String> logs = repository.getLogs(uuid);
		return logs.isEmpty() ? List.of("&c기록이 없습니다.") : logs;
	}

	public boolean checkAndSetCooldown(@NotNull UUID uuid) {
		long now = System.currentTimeMillis();
		if (cooldowns.containsKey(uuid)) {
			long lastTime = cooldowns.get(uuid);
			long secondsPassed = (now - lastTime) / 1000;

			if (secondsPassed < Constants.INQUIRY_COOLDOWN) {
				return true;
			}
		}

		cooldowns.put(uuid, now);
		return false;
	}

	public long getRemainingCooldown(@NotNull UUID uuid) {
		if (!cooldowns.containsKey(uuid)) return 0;
		long secondsPassed = (System.currentTimeMillis() - cooldowns.get(uuid)) / 1000;
		return Math.max(0, Constants.INQUIRY_COOLDOWN - secondsPassed);
	}
}

