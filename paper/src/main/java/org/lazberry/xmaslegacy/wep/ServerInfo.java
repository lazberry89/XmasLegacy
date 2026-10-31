package org.lazberry.xmaslegacy.wep;

public record ServerInfo(
		String name,
		String version,
		int onlineCount,
		int preregCount,
		boolean addressRevealed,
		String address) {
}
