package org.lazberry.xmaslegacy.api.donate;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.lazberry.xmaslegacy.joinListeners.PlayerDataReadyEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.api.ProductRecordManager;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.util.concurrent.CompletableFuture;

/**
 * Grants purchases that arrived while the player was offline (or on the lobby).
 * The online case is handled by {@link DonationGrantSubscriber}.
 */
@Slf4j
@Listeners
@Components.Exclude(type = ServerType.LOBBY)
public class DonationJoinListener implements Listener {
	private final ProductActionManager actionManager;
	private final ProductRecordManager recordManager;

	@Inject
	public DonationJoinListener(ProductActionManager actionManager, ProductRecordManager recordManager) {
		this.actionManager = actionManager;
		this.recordManager = recordManager;
	}

	/** Not on join: HuskSync replaces the inventory right after join and would wipe the reward. */
	@EventHandler
	public void grantPendedProducts(PlayerDataReadyEvent e) {
		var uuid = e.getPlayer().getUniqueId();

		// Redis lookup stays off the main thread; grantProduct is safe to call from any thread.
		CompletableFuture.supplyAsync(() -> recordManager.getPendedIds(uuid))
				.thenAccept(ids -> {
					// shortcut: logs on every ready event to trace offline grants; drop once confirmed working.
					log.info("Pended web products for {}: {}", uuid, ids);
					ids.forEach(actionManager::grantProduct);
				})
				.exceptionally(ex -> {
					log.error("Failed to check pended web products for {}.", uuid, ex);
					return null;
				});
	}
}
