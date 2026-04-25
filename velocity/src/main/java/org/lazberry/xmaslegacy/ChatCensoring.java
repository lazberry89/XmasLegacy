package org.lazberry.xmaslegacy;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;

@SuppressWarnings("ClassCanBeRecord")
public class ChatCensoring {
	private final RuleManager RM;

	public ChatCensoring(RuleManager RM) {
		this.RM = RM;
	}

	@Subscribe
	public void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		String message = event.getMessage();

		// 공백 제거 후 비속어 체크
		String checkStr = message.replaceAll("\\s+", "");

		if (RM.checkBadWords(checkStr)) {
			String filteredMessage = RM.hideBadWords(message);
			event.setResult(PlayerChatEvent.ChatResult.message(filteredMessage));

			player.sendMessage(ColorUtils.chat(Prefix.RED + " 욕설이 포함된 채팅은 제재받을 수 있습니다."));
		}
	}
}