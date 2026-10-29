package org.lazberry.xmaslegacy.chat.whisper;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.UUID;

@Slf4j
@Registry.Include(type = ServerType.GLOBAL)
public class WhisperManager {
    private static final String WHISPER_CHANNEL = "whisper_global";

	public WhisperManager() {}

    public Result sendWhisper(Player sender, String senderServer, String receiver, String message) {
		String senderName = sender.getName();
		UUID senderId = sender.getUniqueId();

		var targetPlayer = Bukkit.getOfflinePlayer(receiver);
		if (!targetPlayer.hasPlayedBefore()) return Result.NOT_EXIST;

		UUID targetPlayerId = targetPlayer.getUniqueId();
		if (JedisService.getUserServer(targetPlayerId) == null) return Result.OFFLINE;

	    JedisService.publish(WHISPER_CHANNEL, new Whisper(senderName, receiver, senderServer,
			    senderId, targetPlayer.getUniqueId(), message).toJson());
		sender.sendMessage(whisperSent(receiver, message));
		logWhisper(senderName, receiver, message);
		return Result.SUCCESS;
    }

	public Component whisperSent(String receiver, String message) {
		return ColorUtils.chat("&b나 -> " + receiver + " &7" + message);
	}

	public Component whisperReceived(String sender, String message) {
		return ColorUtils.chat("&e" + sender + " -> 나 &7" + message);
	}

	public void logWhisper(String sender, String receiver, String message) {
		log.info("{} -> {} {}", sender, receiver, message);
	}

	public enum Result {
		NOT_EXIST, OFFLINE, SUCCESS
	}
}
