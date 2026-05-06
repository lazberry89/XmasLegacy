package xmasLegacy.Lobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import xmasLegacy.XmasLegacy;

public class LobbyListener implements Listener {
    private final XmasLegacy plugin;
    private final LobbyManager LBM;

    public LobbyListener(XmasLegacy plugin, LobbyManager LBM) {
        this.plugin = plugin;
        this.LBM = LBM;
    }

    @EventHandler
    public void onLobbyJoin(PlayerJoinEvent e) {
        if (!plugin.getServerType().equals("lobby")) return;
        Player p = e.getPlayer();
        if (LBM.getSpawn() != null) {
            p.teleport(LBM.getSpawn());
            p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_0, 1.0f, 1.0f);
        } else {
            Component msg = ColorUtils.chat(Prefix.RED + " 당신 위치에 문제가 있어보이네요. 서버측 오류니까 문의해주세요! ")
                            .append(ColorUtils.chat("&c&l[문의]"))
                                    .clickEvent(ClickEvent.runCommand("/문의 서버측 config.yml에 서버등록이 잘못됐어요!"))
                                            .hoverEvent(HoverEvent.showText(ColorUtils.chat("클릭시 해당 오류에 대해 바로 문의할 수 있어요.")));
            p.sendMessage(msg);
            p.playSound(p, Sound.BLOCK_ANVIL_USE, 2.0f, 1.0f);
        }
    }
}
