package org.lazberry.xmaslegacy.api.donate;

import lombok.Getter;
import org.lazberry.xmaslegacy.api.PermissionHandler;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public enum ProductType {
	SEASON_PASS("season_pass"),
	STARTER_PACK("starter_pack"),
	SUPPORTER("supporter"),;

	private final String key;

	ProductType(String key) {
		this.key = key;
	}

	public CompletableFuture<Boolean> grantPermission(UUID uuid) {
		return PermissionHandler.addPermission(uuid, key);
	}

	public CompletableFuture<Boolean> deprivePermission(UUID uuid) {
		return PermissionHandler.removePermission(uuid, key);
	}
}
