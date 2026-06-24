package xmaslegacy.PartyCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

public class PartyCommandHelp implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 1)
            sendHelpMessage(player);
        else InfoUtils.infoMsg(InfoLevel.ERROR, player, "유효하지 않은 명령어입니다.");
    }

    private void sendHelpMessage(@NotNull Player p) {
        p.sendMessage(ColorUtils.chat("&7&m-------------------------------------"));
        p.sendMessage(ColorUtils.chat("&6&l[ XmasLegacy 파티 시스템 사용법 ]"));
        p.sendMessage(ColorUtils.chat("&e/파티 생성 &7- 새로운 파티를 생성합니다."));
        p.sendMessage(ColorUtils.chat("&e/파티 초대 <이름> &7- 플레이어를 파티에 초대합니다."));
        p.sendMessage(ColorUtils.chat("&e/파티 참가 <이름> &7- 해당 플레이어의 파티에 가입합니다."));
        p.sendMessage(ColorUtils.chat("&e/파티 멤버 &7- 현재 소속된 파티원을 확인합니다."));
        p.sendMessage(ColorUtils.chat("&e/파티 추방 <이름> &7- 파티원을 파티에서 쫓아냅니다."));
        p.sendMessage(ColorUtils.chat("&e/파티 나가기 &7- 현재 파티에서 탈퇴합니다."));
        p.sendMessage(ColorUtils.chat("&7&m-------------------------------------"));
    }
}
