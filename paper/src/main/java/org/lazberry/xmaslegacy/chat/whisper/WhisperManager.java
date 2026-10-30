package org.lazberry.xmaslegacy.chat.whisper;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

	/**
	 * Sending whisper to Proxy user.
	 * Because this method uses Jedis query methods, when several players query this command might
	 * occur server tick freezing. That's why this method is synchronized, at least preventing
	 * consequence command use.
	 * @param sender player instance of sender
	 * @param senderServer server that sender is located
	 * @param receiver receiver's name that sender tries to send whisper
	 * @param message contents to send
	 * @return result of sending whisper.
	 */
    public synchronized boolean sendWhisper(Player sender, String senderServer, String receiver, String message) {
		String senderName = sender.getName();
		UUID senderId = sender.getUniqueId();

		OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(receiver);
		UUID targetPlayerId = targetPlayer.getUniqueId();

		if (JedisService.getUserServer(targetPlayerId) == null) return false;

	    JedisService.publish(WHISPER_CHANNEL, new Whisper(senderName, receiver, senderServer,
			    senderId, targetPlayer.getUniqueId(), message).toJson());
		sender.sendMessage(whisperSent(receiver, message));
		logWhisper(senderName, receiver, message);
		return true;
    }

	public Component whisperSent(String receiver, String message) {
		return ColorUtils.chat("&b[ 나 -> " + receiver + " ]&7 " + message);
	}

	public Component whisperReceived(String sender, String message) {
		return ColorUtils.chat("&e[ " + sender + " -> 나 ]&7 " + message);
	}

	private void logWhisper(String sender, String receiver, String message) {
		log.info("[ {} -> {} ] {}", sender, receiver, message);
	}
}
