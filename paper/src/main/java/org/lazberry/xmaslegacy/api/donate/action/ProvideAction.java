package org.lazberry.xmaslegacy.api.donate.action;

import org.bukkit.OfflinePlayer;
import org.lazberry.xmaslegacy.api.donate.ProductType;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class ProvideAction {

	public abstract CompletableFuture<Boolean> grantProduct(UUID uuid);

	public abstract CompletableFuture<Boolean> revokeProduct(UUID uuid);
}
