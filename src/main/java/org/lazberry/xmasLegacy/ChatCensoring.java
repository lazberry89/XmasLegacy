package org.lazberry.xmasLegacy;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ComponentChanger;

public class ChatCensoring implements Listener {
	private final RuleManager RM;
	private final XmasLegacy plugin;

	public ChatCensoring(RuleManager RM, XmasLegacy plugin) {
		this.RM = RM;
		this.plugin = plugin;
	}

	@EventHandler
	public void onChatCensor(AsyncChatEvent e) {
		Player p = e.getPlayer();
		String msg = PlainTextComponentSerializer.plainText().serialize(e.message());

		if (RM.checkBadWords(msg)) {
			String replace = RM.hideBadWords(msg);
			e.message(ComponentChanger.comp(replace));

			Bukkit.getScheduler().runTask(plugin, () -> {
				p.sendMessage(ColorUtils.chat(Prefix.RED + " 욕설이 사용된 채팅은 재제받을 수 있습니다."));
				p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
			});
		}
	}
}
