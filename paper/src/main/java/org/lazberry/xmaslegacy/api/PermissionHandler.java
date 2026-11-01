package org.lazberry.xmaslegacy.api;

import lombok.extern.slf4j.Slf4j;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class PermissionHandler {
	private static final LuckPerms luckPerms = LuckPermsProvider.get();

	public static boolean hasPermission(UUID uuid, String node) {
		synchronized (PermissionHandler.class) {
			var user = luckPerms.getUserManager().getUser(uuid);
			return user != null && user.getCachedData().getPermissionData().checkPermission(node).asBoolean();
		}
	}

	public static CompletableFuture<Boolean> addPermission(UUID uuid, String node) {
		return luckPerms.getUserManager().loadUser(uuid).thenCompose(user -> {
			if (user == null) return CompletableFuture.completedFuture(false);
			user.data().add(Node.builder(node).build());

			return luckPerms.getUserManager().saveUser(user)
					.thenApply(v -> true)
					.exceptionally(ex -> {
						log.error("Failed to save permissions for user: {}", uuid, ex);
						return false;
					});
		});
	}

	public static CompletableFuture<Boolean> removePermission(UUID uuid, String node) {
		return luckPerms.getUserManager().loadUser(uuid).thenCompose(user -> {
			if (user == null) return CompletableFuture.completedFuture(false);
			user.data().remove(Node.builder(node).build());

			return luckPerms.getUserManager().saveUser(user)
					.thenApply(v -> true)
					.exceptionally(ex -> {
						log.error("Failed to remove permissions for user: {}", uuid, ex);
						return false;
					});
		});
	}
}
