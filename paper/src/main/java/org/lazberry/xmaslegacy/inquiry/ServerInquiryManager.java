package org.lazberry.xmaslegacy.inquiry;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.RuleManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Registry.Include(type = ServerType.GLOBAL)
public class ServerInquiryManager extends AbstractInquirySubscriber implements Initiator {
    private static final NamespacedKey key = KeyUtils.get("inquiry-alert");
    private final Map<UUID, Set<Long>> unreadAnswers = new ConcurrentHashMap<>();
    private final InquiryManager inquiryManager;
    private final RuleManager ruleManager;

    @Inject
    public ServerInquiryManager(InquiryManager inquiryManager, RuleManager ruleManager) {
        this.inquiryManager = inquiryManager;
        this.ruleManager = ruleManager;
    }

    @Override
    public void init() {
        subscribeJedisService(this);
    }

    public void sendInquiry(Player player, String message) {
        var uuid = player.getUniqueId();
        if (inquiryManager.canInquire(uuid)) {
            String inquiry = message;
            if (ruleManager.checkBadWords(message)) {
                inquiry = ruleManager.hideBadWords(message);
                InfoUtils.error(player, "욕설이 포함된 문의는 제재될 수 있습니다.");
            }
            inquiryManager.sendInquiry(player.getName(), uuid, inquiry);
        } else InfoUtils.warn(player, String.format("아직 문의할 수 없습니다. &7(%d초 남음)", (int) inquiryManager.getInquiryCooldown(uuid)));
    }

    public void answerInquiry(Player p, long id, String message) {
        Inquiry inquiry = inquiryManager.getInquiry(id);
        if (inquiry == null) {
            InfoUtils.error(p, "존재하지 않는 문의입니다.");
            return;
        }
        String answer = ruleManager.hideBadWords(message);
        inquiryManager.answerInquiry(id, answer);
        InfoUtils.info(p, "등록하신 문의 &e#{}&f가 답변되었습니다.", id);
    }

    @Override
    protected void onInquiryReceived(String playerName, String ticketId) {
        Component alert = ColorUtils.chat("&3&l#" + ticketId + " &f" + playerName + "님의 문의");
        forEachActiveOperators(p -> {
            if (isAlertBlock(p)) return;
            p.sendMessage(alert);
            p.playSound(p, Sound.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f);
        });
    }

    @Override
    protected void onInquiryAnswered(UUID userUuid, String ticketId) {
        try {
            long value = Long.parseLong(ticketId);

            String message = String.format("문의 &e#%s&f가 답변되었습니다. &6'/문의함'&f에서 확인하세요.", value);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(userUuid);
            if (offlinePlayer.isOnline()) {
                var player = offlinePlayer.getPlayer();
                if (player == null) {
                    unreadAnswers.computeIfAbsent(userUuid, k -> new HashSet<>()).add(value);
                    return;
                }

                InfoUtils.info(player, ColorUtils.chat(message));
            } else {
                unreadAnswers.computeIfAbsent(userUuid, k -> new HashSet<>()).add(Long.getLong(ticketId));
            }
        } catch (NumberFormatException e) {
            log.error("Failed to send inquiry alert to target player. (id: {})", ticketId);
        }
    }

    public static boolean isAlertBlock(Player p) {
        return Boolean.FALSE.equals(KeyUtils.get(p, key, PersistentDataType.BOOLEAN));
    }

    public static void setAlertState(Player p, boolean value) {
        KeyUtils.set(p, key, value);
    }

    private void forEachActiveOperators(Consumer<Player> consumer) {
        Bukkit.getOnlinePlayers().stream()
                .filter(Player::isValid)
                .filter(Player::isOnline)
                .forEach(consumer);
    }
}
