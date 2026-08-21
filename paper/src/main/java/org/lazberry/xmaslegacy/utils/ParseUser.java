package org.lazberry.xmaslegacy.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.User.User;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ParseUser {

	public static CompletableFuture<Player> parse(UUID uuid) {
		return CompletableFuture.supplyAsync(() -> Bukkit.getPlayer(uuid));
	}

	public static CompletableFuture<Player> parse(User user) {
		if (user == null) return CompletableFuture.completedFuture(null);
		return CompletableFuture.supplyAsync(() -> Bukkit.getPlayer(user.getUniqueId()));
	}
}
