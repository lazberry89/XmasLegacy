package org.lazberry.xmaslegacy.bags;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.bags.database.SqlBagRepository;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.joinListeners.PlayerDataReadyBridge;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.InventorySerializer;
import redis.clients.jedis.JedisPubSub;

import java.util.UUID;

/**
 * Delivers items to players who are not on the server giving them (offline, or on another server).
 * <p>
 * Items go into the {@code bag_mailbox} table, never into the bag itself, because the bag of a
 * player who is online elsewhere is owned by that server and would be overwritten on its next save.
 * Only the server the player is on claims the mailbox: right after the bag loads on join, or
 * immediately when another server announces new mail over Redis.
 */
@Slf4j
@Components.Exclude(type = ServerType.LOBBY)
public class BagMailbox extends JedisPubSub implements Initiator {
	private static final String CHANNEL = "bag:mailbox";

	private final SqlBagRepository repository;
	private final BagManager bagManager;
	private final PlayerDataReadyBridge dataReady;
	private final XmasLegacy plugin;

	@Inject
	public BagMailbox(SqlBagRepository repository, BagManager bagManager, PlayerDataReadyBridge dataReady, XmasLegacy plugin) {
		this.repository = repository;
		this.bagManager = bagManager;
		this.dataReady = dataReady;
		this.plugin = plugin;
	}

	/**
	 * Delivers only once both the bag is loaded (overflow goes there) and the synced inventory is in place
	 * (HuskSync would overwrite anything given before). Call it whenever either becomes true.
	 */
	public void tryDeliver(Player player) {
		var uuid = player.getUniqueId();
		if (bagManager.isLoaded(uuid) && dataReady.isReady(uuid)) deliver(player);
	}

	@Override
	public void init() {
		JedisService.subscribe(this, CHANNEL);
	}

	@Override
	public void close() {
		JedisService.unsubscribe(this);
	}

	/** Stores the items for this player and tells whichever server they are on to collect them. */
	public void send(UUID uuid, ItemStack... items) {
		repository.addToMailbox(uuid, items).thenRun(() -> JedisService.publish(CHANNEL, uuid.toString()))
				.exceptionally(ex -> {
					// Logged with the payload so an admin can restore the items by hand.
					log.error("Failed to store mailbox items for {}. Lost items: {}",
							uuid, InventorySerializer.serializeContents(items), ex);
					return null;
				});
	}

	/** Moves everything waiting in the mailbox into the player's inventory (overflow into the bag). */
	public void deliver(Player player) {
		var uuid = player.getUniqueId();
		repository.claimMailbox(uuid).thenAccept(items -> {
			if (items.isEmpty()) return;
			Bukkit.getScheduler().runTask(plugin, () -> {
				var array = items.toArray(ItemStack[]::new);
				if (!player.isOnline()) {
					// Left while claiming: put the items back instead of dropping them.
					send(uuid, array);
					return;
				}
				InventoryHelper.giveItemOrKeep(player, array);
				InfoUtils.info(player, "보관되어 있던 아이템 &6{}&f개를 받았습니다.", array.length);
			});
		}).exceptionally(ex -> {
			log.error("Failed to claim mailbox for {}. Items stay in the mailbox.", uuid, ex);
			return null;
		});
	}

	@Override
	public void onMessage(String channel, String message) {
		if (!CHANNEL.equals(channel)) return;

		UUID uuid;
		try {
			uuid = UUID.fromString(message);
		} catch (IllegalArgumentException e) {
			return; // an exception here would end the subscription
		}

		Bukkit.getScheduler().runTask(plugin, () -> {
			Player player = Bukkit.getPlayer(uuid);
			// Not ready yet: the bag-load or data-ready path delivers later, so nothing is missed.
			if (player != null) tryDeliver(player);
		});
	}
}
