package org.lazberry.xmaslegacy.utils;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.ServerPrefix.PrefixManager;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserSaveManager;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Alert;

import java.util.function.BiConsumer;

/**
 * Utils for {@link Player} that struggles for {@link User} loading bugs.
 * This util sends click-able Componented message
 * to intended player that the player could reload his/her user info Easily, improving UI/UX.
 * This Util is used in two cases.
 * First, when using {@link UserHandler#loadUser(Player, boolean)}, using flag msg,
 * you can hide info message and only process loading.
 * <pre>{@code
 * if (user == null) {
 *     UserHandler.loadUser(p, false);
 *     p.sendMessage("Secretly loaded your info!");
 * }
 * }</pre>
 * Second, use {@link UserHandler#sendReloadNotice(Player)} to inform player that his/her information is not valid,
 * and should reload.
 * <pre>{@code
 * if (user == null) {
 *     UserHandler.sendReloadNotice(p);
 *     p.sendMessage("Reload sent. You can choose do or not.");
 * }
 * }</pre>
 * @see Component
 * @see User
 * @see Player
 */
@Slf4j
public final class UserHandler {
	private static @Setter UserSaveManager us;
	private static @Setter PrefixManager pfm;

    /**
     * Same as other utils, block the constructor to be called by others.
     * Throws Exception when someone try to open private and call this constructor.
     */
    @ApiStatus.Internal
    @Contract("-> fail")
    private UserHandler() {
        throw new UnsupportedOperationException("Utility Class");
    }

    @Contract(pure = true)
    private static @NotNull XmasLegacy plugin() {
        return XmasLegacy.getInstance();
    }

    /**
     * Loading {@link User} info from {@link Player}, player doesn't have choice
     * to select to reload or not.
     * Boolean flag only decides to notice or not the player that his/her User info has been loaded.
     * Run using {@link java.util.concurrent.CompletableFuture#whenComplete(BiConsumer)} type of Async
     * @param player target player to load
     * @param msg notice the player or not
     * @see java.util.concurrent.CompletableFuture
     * @see Player
     * @see User
     */
    @NonBlocking
    public static void loadUser(@NotNull Player player, boolean msg) {
        us.onJoinAsync(player.getUniqueId(), player.getName(), true).whenComplete((user, throwable) -> {
            if (throwable != null || user == null) {
                if (msg) sendError(player, throwable);
                return;
            }
            pfm.removePrefixIfNotValid(user);
            if (msg) sendMsg(player, user);
        });
    }

    /**
     * Send click-able {@link Component} alert that player's user info is not loaded, and the target player
     * can choose load or not. Also using {@link ClickCallback.Options}, prevent player from making bug
     * by infinitely using button.
     * @param player target player to send notice Component
     * @see Component
     * @see ClickCallback.Options
     */
    public static void sendReloadNotice(@NotNull Player player) {
        if (player.isOnline() && player.isValid())
            player.sendMessage(ColorUtils.chat(Alert.RED + " 유저 정보가 로드되지 않았습니다.").append(reloadComponent()));
    }

	/**
	 * Option that prevents player spamming reload button.
	 * @return {@link ClickCallback.Options} of max use 1 time, lifetime 3 minutes.
	 */
    @Contract(value = "-> !null", pure = true)
    private static @NotNull ClickCallback.Options option() {
        return ClickCallback.Options.builder()
                .uses(1)
                .lifetime(java.time.Duration.ofMinutes(3))
                .build();
    }

	/**
	 * Component that would be appended to inform message.
	 * This Component contains {@link HoverEvent} and
	 * {@link ClickEvent#callback(ClickCallback)} logic, so when player click this component,
	 * the logic will work.
	 * @return Component contains ClickEvent, HoverEvent.
	 * @see Component
	 * @see HoverEvent
	 * @see ClickEvent
	 */
    @Contract(value = "-> !null", pure = true)
    private static @NotNull Component reloadComponent() {
        return ColorUtils.chat(" &c&l[ 다시 로드하기 ]")
                .hoverEvent(HoverEvent.showText(ColorUtils.chat("&c&l클릭하여 유저 정보를 다시 로드합니다.")))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player t && t.isOnline()) {
                        loadUser(t, true);
                    }
                }, option()));
    }

	/**
	 * Method that handles sending message. This method contains first join message, general Join message.
	 * @param player target player
	 * @param user {@link User} that parsed from player
	 */
    private static void sendMsg(@NotNull Player player, @NotNull User user) {
        Bukkit.getScheduler().runTask(plugin(), () -> {
            if (!player.isOnline()) return;

            if (user.isNewUser()) {
                Bukkit.broadcast(ColorUtils.chat(String.format(Alert.XmasLegacy + "&6&l %s&f 님의 첫 접속입니다. 환영해주세요!\uD83C\uDF84", player.getName())));
                player.playSound(player, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                player.spawnParticle(Particle.SOUL_FIRE_FLAME, player.getLocation().add(0, 1, 0), 5, 0.5, 1.2, 0.5, 0.01);

                if (FloodgateUtils.isFloodgate(player)) {
                    user.addDollars(Constants.BASIC_MONEY_MOBILE);
                    player.sendMessage(ColorUtils.chat(Alert.GREEN + " 모바일 접속 보너스가 지급되었습니다."));
                } else user.addDollars(Constants.BASIC_MONEY_NORMAL);
            } else Bukkit.broadcast(ColorUtils.chat(String.format(Alert.XmasLegacy + "&6&l %s&f 님이 접속했어요!", player.getName())));
            //UserTagManager.createHoverTag(player, user);
            //UserTagManager.updateHoverTag(player, user);
            user.setNewUser(false);
        });
    }

	/**
	 * Used when inner exception occurred. Logs error, and send alert to target player.
	 * @param player target player
	 * @param throwable e
	 */
    private static void sendError(@NotNull Player player, Throwable throwable) {
        Bukkit.getScheduler().runTask(plugin(), () -> {
            if (!player.isOnline()) return;
            player.sendMessage(ColorUtils.chat(Alert.RED + " 유저 정보 로드 중 시스템 내부 예외가 발생했습니다.").append(reloadComponent()));
            log.error("비동기 유저 로드 중 치명적 예외 발생 (UUID: {})", player.getUniqueId(), throwable);
        });
    }
}
