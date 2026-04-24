package org.lazberry.xmaslegacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InquiryManager {
	private final Map<UUID, Long> cooldowns = new HashMap<>();
	private final Map<UUID, String> inquiryMessage = new HashMap<>();
	private final int cooldownTime = Constants.INQUIRY_COOLDOWN;
	private final UserManager um;

	public InquiryManager(UserManager um) {
		this.um = um;
	}

	public Component Inquiry(UUID uuid, @Nullable String message) {
		User user = um.getUser(uuid);
		String name = (user != null) ? user.getName() : "Unknown";
		if (message == null) {
			inquiryMessage.put(uuid, "내용 없는 호출");
			return ComponentChanger.comp("이동하시겠습니까? ")
					.append(ComponentChanger.comp("&a&l[이동]"))
					.clickEvent(ClickEvent.runCommand(STR."/이동문의 \{name}"))
					.hoverEvent(HoverEvent.showText(ComponentChanger.comp("클릭 시 문의한 유저의 위치로 이동합니다.")));
		} else {
			inquiryMessage.put(uuid, message);
			Component msg = ComponentChanger.comp("이동하시겠습니까? ");
			Component nt = ComponentChanger.comp("&a&l[이동]")
					.clickEvent(ClickEvent.runCommand(STR."/이동문의 \{name}"))
					.hoverEvent(HoverEvent.showText(ComponentChanger.comp(STR."\{Prefix.YELLOW} 클릭 시 문의한 유저의 위치로 이동합니다.")));
			Component bwt = ComponentChanger.comp(" &c&l[처벌]")
					.clickEvent(ClickEvent.runCommand(STR."/ban \{name} 욕설사용"))
					.hoverEvent(HoverEvent.showText(ComponentChanger.comp(STR."\{Prefix.RED} 클릭 시 유저를 밴 처리합니다.")));
			msg = msg.append(nt);
			return msg;
		}
	}

	public int getCooldownTime() {
		return cooldownTime;
	}
}
