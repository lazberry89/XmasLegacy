package org.lazberry.xmaslegacy.api;

import lombok.Getter;

import org.lazberry.xmaslegacy.api.PermissionHandler;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.lazberry.xmaslegacy.api.donate.action.ProvideAction;

@Getter
public enum ProductType {
	SEASON_PASS("season_pass", false),
	STARTER_PACK("starter_pack", false),
	SUPPORTER("supporter", false),;

	private final String key;
	private final boolean canDuplicated;
	private final Class<? extends ProvideAction> actionClass;

	ProductType(String key, boolean canDuplicated, Class<? extends ProvideAction> actionClass) {
		this.key = key;
		this.canDuplicated = canDuplicated;
		this.actionClass = actionClass;
	}

	public CompletableFuture<Boolean> grantPermission(UUID uuid) {
		return PermissionHandler.addPermission(uuid, key);
	}

	public CompletableFuture<Boolean> deprivePermission(UUID uuid) {
		return PermissionHandler.removePermission(uuid, key);
	}
}
