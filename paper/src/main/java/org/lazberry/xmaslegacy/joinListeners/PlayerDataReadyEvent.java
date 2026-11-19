package org.lazberry.xmaslegacy.joinListeners;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired on the main thread once the player's synced data (inventory etc.) is in place on this server.
 * Give items here, not on PlayerJoinEvent: HuskSync overwrites the inventory a moment after join.
 * @see PlayerDataReadyBridge
 */
public class PlayerDataReadyEvent extends PlayerEvent {
	private static final HandlerList HANDLERS = new HandlerList();

	public PlayerDataReadyEvent(@NotNull Player player) {
		super(player);
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
