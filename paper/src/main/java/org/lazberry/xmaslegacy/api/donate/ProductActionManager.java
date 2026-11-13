package org.lazberry.xmaslegacy.api.donate;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.api.ProductRecordManager;
import org.lazberry.xmaslegacy.api.ProductType;
import org.lazberry.xmaslegacy.api.donate.action.ProvideAction;
import org.lazberry.xmaslegacy.api.donate.action.StarterPackAction;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.util.EnumMap;
import java.util.Map;

@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class ProductActionManager implements Initiator {
	private final Map<ProductType, ProvideAction> typeActions = new EnumMap<>(ProductType.class);
	private final XmasLegacy plugin;
	private final ProductRecordManager recordManager;

	@Inject
	public ProductActionManager(XmasLegacy plugin, ProductRecordManager recordManager) {
		this.plugin = plugin;
		this.recordManager = recordManager;
		typeActions.put(ProductType.STARTER_PACK, new StarterPackAction(plugin));
	}

	@Override
	public void init() {

	}

	/**
	 * Grants the purchase with this purchase number, if its receiver is online on this server.
	 * Safe to call from any thread. Otherwise, the purchase simply stays ungranted.
	 */
	public void grantProduct(long id) {
		recordManager.find(id).thenAccept(product -> {
			if (product == null) return;

			var action = typeActions.get(product.getProduct());
			if (action == null) {
				log.warn("No grant action for {} (id: {}). Left ungranted.", product.getProduct(), id);
				return;
			}

			// Every paper server gets the signal, so check the player is here BEFORE claiming.
			Bukkit.getScheduler().runTask(plugin, () -> {
				if (Bukkit.getPlayer(product.getReceiver()) == null) return;

				recordManager.markAsGranted(product).thenAccept(claimed -> {
					if (!claimed) return;

					Bukkit.getScheduler().runTask(plugin, () -> {
						var player = Bukkit.getPlayer(product.getReceiver());
						if (player == null) {
							// Left during the claim: give the purchase back instead of losing it.
							recordManager.revertGrant(product);
							return;
						}
						try {
							action.grantProduct(player);
							log.info("Granted web product. (id: {}, receiver: {}, type: {})",
									id, product.getReceiver(), product.getProduct());
						} catch (Exception e) {
							// TODO: recover purchases marked granted in the database whose reward was never given.
							log.error("Web product marked granted but reward failed! (id: {}, receiver: {}, type: {})",
									id, product.getReceiver(), product.getProduct(), e);
						}
					});
				}).exceptionally(ex -> {
					log.error("Failed to claim web product {}.", id, ex);
					return null;
				});
			});
		}).exceptionally(ex -> {
			log.error("Failed to look up web product {}.", id, ex);
			return null;
		});
	}

	@Override
	public void close() {
		typeActions.clear();
	}
}
