package org.lazberry.xmaslegacy.chat.whisper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.UUID;

public record Whisper(String sender,
                      String receiver,
					  String senderServer,
                      UUID senderId,
                      UUID receiverId,
                      String contents) {
	private static final NamespacedKey key = KeyUtils.get("whisper_mute");
	private static final Gson GSON = new GsonBuilder().create();

	public String toJson() {
		return GSON.toJson(this);
	}

	public static Whisper fromJson(String json) {
		if (json == null || json.isEmpty()) return null;
		return GSON.fromJson(json, Whisper.class);
	}

	public static void mute(Player player) {
		KeyUtils.set(player, key, true);
	}

	public static void unmute(Player player) {
		KeyUtils.set(player, key, false);
	}

	public static boolean isMuted(Player player) {
		return Boolean.TRUE.equals(KeyUtils.get(player, key, PersistentDataType.BOOLEAN));
	}
}
