package org.lazberry.xmaslegacy.joinListeners;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Turns "player data is synced" into {@link PlayerDataReadyEvent}.
 * With HuskSync it waits for HuskSync's sync-complete event; without it, join is already the ready point.
 * HuskSync is hooked by reflection so the plugin builds and runs without it.
 */
@Slf4j
@Listeners
@Components.Exclude(type = ServerType.LOBBY)
public class PlayerDataReadyBridge implements Listener, Initiator {
	private static final String HUSKSYNC_EVENT = "net.william278.husksync.event.BukkitSyncCompleteEvent";

	private final XmasLegacy plugin;
	private final Set<UUID> ready = ConcurrentHashMap.newKeySet();
	private boolean waitForHuskSync = false;

	@Inject
	public PlayerDataReadyBridge(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	@Override
	public void init() {
		var huskSync = Bukkit.getPluginManager().getPlugin("HuskSync");
		if (huskSync == null || !huskSync.isEnabled()) return;

		try {
			@SuppressWarnings("unchecked")
			Class<? extends Event> type = (Class<? extends Event>)
					Class.forName(HUSKSYNC_EVENT, true, huskSync.getClass().getClassLoader());

			Bukkit.getPluginManager().registerEvent(type, this, EventPriority.MONITOR, (listener, event) -> {
				if (!type.isInstance(event)) return;
				UUID uuid = uuidOf(event);
				// HuskSync may fire this off the main thread; everything downstream expects the main thread.
				if (uuid != null) Bukkit.getScheduler().runTask(plugin, () -> markReady(uuid));
			}, plugin);

			waitForHuskSync = true;
			log.info("HuskSync found: items are given after its data sync completes.");
		} catch (ReflectiveOperationException | ClassCastException e) {
			log.error("Failed to hook HuskSync sync event. Falling back to join; items given on join may be overwritten.", e);
		}
	}

	private static @Nullable UUID uuidOf(Event event) {
		try {
			Object user = event.getClass().getMethod("getUser").invoke(event);
			return (UUID) user.getClass().getMethod("getUuid").invoke(user);
		} catch (ReflectiveOperationException | ClassCastException e) {
			log.error("Could not read the player from HuskSync's sync event.", e);
			return null;
		}
	}

	/** True once this player's data is synced on this server (and until they leave). */
	public boolean isReady(UUID uuid) {
		return ready.contains(uuid);
	}

	private void markReady(UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		if (player == null || !ready.add(uuid)) return;
		Bukkit.getPluginManager().callEvent(new PlayerDataReadyEvent(player));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void readyOnJoinWithoutHuskSync(PlayerJoinEvent e) {
		if (!waitForHuskSync) markReady(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void forgetOnQuit(PlayerQuitEvent e) {
		ready.remove(e.getPlayer().getUniqueId());
	}

	@Override
	public void close() {
		ready.clear();
	}
}
