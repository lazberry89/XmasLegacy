package xmasLegacy;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;

@SuppressWarnings("unused")
public class ServerTransfer {
    private static final @NotNull XmasLegacy plugin = XmasLegacy.getInstance();

    public ServerTransfer() {}

    public static void loadUser() {

    }

    public static boolean transfer(@NotNull Player player, @NotNull ServerType toServer, boolean force, boolean hide) {
        if (!force) {
            player.sendMessage(askComponent(toServer, hide));
            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            return true;
        }
        return transfer(player, toServer);
    }

    @CheckReturnValue
    private static boolean transfer(@NotNull Player player, @NotNull ServerType toServer) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(toServer.str());

        try {
            player.sendPluginMessage(plugin, "bungeecord:main", out.toByteArray());
        } catch (IllegalArgumentException e) {
            plugin.getSLF4JLogger().error("Error occurred while transferring player {}", player, e);
            return false;
        }
        return true;
    }

    private static @NotNull Component askComponent(@NotNull ServerType type, boolean hide) {
        var options = ClickCallback.Options.builder()
                .uses(1)
                .lifetime(java.time.Duration.ofMinutes(3))
                .build();
        Component btn = ColorUtils.chat("&a&l[수락]").hoverEvent(HoverEvent.showText(ColorUtils.chat("클릭하여 이동하세요. _ -> &6" + (hide ? "&k???" : type.name()))))
                .clickEvent(ClickEvent.callback(audience -> {
                    if (audience instanceof Player p && p.isOnline()) transfer(p, type);
                }, options));
        Component msg = ColorUtils.chat(Alert.XmasLegacy + " &6서버이동&f 제안이 왔습니다. 이동하시겠습니까? ");
        return msg.append(btn);
    }
}