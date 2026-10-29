package org.lazberry.xmaslegacy.chat.whisper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.UUID;

public record Whisper(String sender,
                      String receiver,
					  String senderServer,
                      UUID senderId,
                      UUID receiverId,
                      String contents) {
	private static final Gson GSON = new GsonBuilder().create();

	public String toJson() {
		return GSON.toJson(this);
	}

	public static Whisper fromJson(String json) {
		if (json == null || json.isEmpty()) return null;
		return GSON.fromJson(json, Whisper.class);
	}
}
