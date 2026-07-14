package org.lazberry.xmaslegacy.Utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;
import org.jetbrains.annotations.*;

import java.util.UUID;

/**
 * Plugin that uses this class should Depend on {@link org.bukkit.plugin.Plugin} "Floodgate"
 * this class handles getting mobile player, or figuring out if player is mobile.
 * But, this logic doesn't throw any {@link Exception} even if floodgate plugin is not loaded Successfully. Only returns
 * false when Api is not valid or plugin is not loaded.
 * <pre>{@code
 * if (isFloodgate(uuid)) {
 *     //...logic
 * }
 * if (isFloodgate(player)) {
 *     //..logic (overloaded version)
 * }
 * }</pre>
 * @see FloodgateApi
 * @see Player
 * @see UUID
 */
public final class FloodgateUtils {
    private static final @NotNull FloodgateApi instance = FloodgateApi.getInstance();

    /**
     * There would be a simple way using @UtilityClass annotation to
     * block calling the constructor, but I like coding in Basic ways.
     * not other reasons~
     */
    @Contract("-> fail")
    @ApiStatus.Internal
    private FloodgateUtils() {
        throw new UnsupportedOperationException("Utility Class");
    }

    /**
     * Figuring out that {@link Player} is Mobile(floodgate). But as side effect, this method return only false
     * when floodgate plugin is not successfully loaded. Reusing method {@link FloodgateUtils#isFloodgate(UUID)}
     * @param player target entity
     * @return whether that entity is mobile or not
     * @see Player
     * @see FloodgateUtils#isFloodgate(UUID)
     */
    @CheckReturnValue
    @Contract(pure = true)
    public static boolean isFloodgate(@NotNull Player player) {
        return isFloodgate(player.getUniqueId());
    }

    /**
     * Figuring out that target {@link UUID} is from floodgate. As side effect, this method only returns false when
     * plugin floodgate is not successfully loaded instead of {@link Exception}
     * @param uuid target UniqueId
     * @return whether that uuid is from mobile or not
     * @see FloodgateUtils#isFloodgate(Player)
     * @see UUID
     */
    @CheckReturnValue
    @Contract(pure = true)
    public static boolean isFloodgate(@NotNull UUID uuid) {
        return Bukkit.getPluginManager().isPluginEnabled("floodgate")
                && instance.isFloodgatePlayer(uuid);
    }

    /**
     * Finding {@link FloodgatePlayer} from {@link FloodgateApi}.
     * General {@link Player} is Different withFloodgate player, so you intend to use floodgate methods,
     * Use this method to get FloodgatePlayer instance. Also, this method can convert Player to FloodgatePlayer by
     * getting {@link UUID} from Player instance.
     * @param uuid target UniqueId
     * @return FloodgatePlayer instance, null if fails.
     * @see FloodgatePlayer
     * @see Player
     */
    @CheckReturnValue
    @Contract("_ -> null")
    public static @Nullable FloodgatePlayer getFloodgatePlayer(@NotNull UUID uuid) {
        if (!isFloodgate(uuid)) return null;
        return instance.getPlayer(uuid);
    }
}
