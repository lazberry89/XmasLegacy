package xmaslegacy.SavingLocation.Lobby;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.PluginUtils.ServerInitializer;
import xmaslegacy.PluginUtils.ServerType;
import xmaslegacy.SavingLocation.DestinationType;
import xmaslegacy.SavingLocation.SavedLocation;

public final class LobbyManager extends SavedLocation {

    public LobbyManager() {
        super(DestinationType.LOBBY);
    }

    public void lobbyJoin(PlayerJoinEvent e) {
        if (!ServerInitializer.getServerType().equals(ServerType.LOBBY)) return;
        Player p = e.getPlayer();
        var spawn = super.getSpawn();
        if (spawn != null) {
            p.teleport(spawn);
            p.playSound(p, Sound.ITEM_GOAT_HORN_SOUND_0, 1.0f, 1.0f);
        } else {
            Component msg = ColorUtils.chat(Alert.RED + " 당신 위치에 문제가 있어보이네요. 서버측 오류니까 문의해주세요! ")
                    .append(ColorUtils.chat("&c&l[문의]"))
                    .clickEvent(ClickEvent.runCommand("/문의 서버측 config.yml에 서버등록이 잘못됐어요!"))
                    .hoverEvent(HoverEvent.showText(ColorUtils.chat("클릭시 해당 오류에 대해 바로 문의할 수 있어요.")));
            p.sendMessage(msg);
            p.playSound(p, Sound.BLOCK_ANVIL_USE, 2.0f, 1.0f);
        }
    }
}