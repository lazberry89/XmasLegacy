package org.lazberry.xmaslegacy.chat.whisper;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

@Registry.Include(type = ServerType.GLOBAL)
public class ChatWhisperSubscriber extends JedisPubSub implements Initiator {
	private static final String WHISPER_CHANNEL = "whisper_global";
	private final WhisperManager whisperManager;

	@Inject
	public ChatWhisperSubscriber(WhisperManager whisperManager) {
		this.whisperManager = whisperManager;
	}

	@Override
	public void init() {
		JedisService.subscribe(this, WHISPER_CHANNEL);
	}

	@Override
	public void close() {
		JedisService.unsubscribe(this);
	}

	@Override
	public void onMessage(String channel, String message) {
		if (!WHISPER_CHANNEL.equals(channel)) return;

		Whisper receivedWhisper = Whisper.fromJson(message);
		if (receivedWhisper == null) return;

		UUID uuid = receivedWhisper.receiverId();
		Player player = Bukkit.getPlayer(uuid);
		if (player != null && player.isOnline())
			whisperManager.whisperReceived(receivedWhisper.sender(), receivedWhisper.contents());

	}
}
