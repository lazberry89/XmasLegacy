package org.lazberry.xmaslegacy.api.wep;

public record ServerInfo(
		String name,
		String version,
		int onlineCount,
		int preregCount,
		boolean addressRevealed,
		String address) {
}
