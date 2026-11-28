package org.lazberry.xmaslegacy.advancement;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

/**
 * Entry point for granting from anywhere. Must not mention UltimateAdvancementAPI: NPCs and teleporters also run
 * on the lobby, which does not have that plugin. There, and before the tab exists, granting does nothing.
 */
public final class Advancements {
	private static volatile @Nullable BiConsumer<Player, String> granter;

	private Advancements() {}

	static void setGranter(@Nullable BiConsumer<Player, String> newGranter) {
		granter = newGranter;
	}

	/** Grants once; ignored if the player already has it. Any thread. */
	public static void grant(@Nullable Player player, @NotNull String id) {
		var current = granter;
		if (current != null && player != null) current.accept(player, id);
	}
}
