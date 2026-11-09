package org.lazberry.xmaslegacy.api.donate.action;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class ProvideAction {

	public abstract CompletableFuture<Boolean> grantProduct(UUID uuid);

	public abstract CompletableFuture<Boolean> revokeProduct(UUID uuid);

	public void recall() {

	}
}
