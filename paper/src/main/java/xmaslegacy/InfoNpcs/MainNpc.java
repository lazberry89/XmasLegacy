package xmaslegacy.InfoNpcs;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Economy.Currency.CurrencyManager;
import xmaslegacy.Utils.GlowUtils;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.ServerTransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@DefaultQualifier(NotNull.class)
public class MainNpc extends AbstractNpc {
    private final @Getter NpcType type = NpcType.MAIN;
    private final @Getter NamespacedKey checkKey;
    private final @Getter Component name = ColorUtils.chat("&6크리아 마을 주민");

    public MainNpc() {
        super(new ArrayList<>(List.of(
                "반갑구만. 여기 마을은 처음인가?",
                "그전에, 여기 오래있다간 얼어죽을거야. 이 음식을 들고가게.",
                "그 음식은 앞으로 상점에서 구매할 수 있을거야. 물론 농부가 일을 잘해준다면 말이지.",
                "이 앞으로 길을 따라가면 당신의 직업을 정해볼 수 있을거야.",
                "진지하게 선택하게. 전직말고는 한번 직업은 평생 직업이기에.",
                "마을 중앙 신전에 도착하면 거기서 누군가 도와줄거야.")
        ));
        this.checkKey = getPlugin().getNamespacedKey("check");
    }

    @Override
    protected String next(Player player) {
        var uuid = player.getUniqueId();
        int num = this.playerCaption.getOrDefault(uuid, 0);
        String currentCaption = this.caption.get(num);

        num++;

        if (num >= this.caption.size()) {
            num = 0;
            var container = player.getPersistentDataContainer();
            if (!Boolean.TRUE.equals(container.get(checkKey, PersistentDataType.BOOLEAN))) {
                container.set(checkKey, PersistentDataType.BOOLEAN, true);
                player.getInventory().addItem(CurrencyManager.currency(5));
                getPlugin().infoMsg(InfoLevel.INFO, player, "재화를 클릭하여 현금 입금을 해보세요!");
            }
        }

        this.playerCaption.put(uuid, num);

        return currentCaption;
    }

    @Override
    public void sendCaption(Player player) {
        var user = UserManager.INSTANCE.getUser(player.getUniqueId());
        if (user == null) {
            ServerTransfer.sendReloadNotice(player);
            return;
        }

        player.playSound(player, Sound.ENTITY_VILLAGER_AMBIENT, 1.0f, 1.0f);
        Component txt = this.name.append(ColorUtils.chat(" &f" + next(player)));
        player.sendActionBar(txt);
        if (user.isMobile()) player.sendMessage(txt);
    }
}
