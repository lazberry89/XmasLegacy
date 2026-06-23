package xmaslegacy.HuntingZone.ZoneCommands;

import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmaslegacy.HuntingZone.HuntingZoneManager;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

import java.util.Arrays;

public class ZoneCommandGet implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var hzm = HuntingZoneManager.INSTANCE;
        if (args.length >= 2) {
            var zone = hzm.getZone(args[1]);

            if (zone == null) {
                InfoUtils.infoMsg(InfoLevel.ERROR, player, "해당 사냥터가 설정되지 않았거나 적절하지 않습니다.");
                return;
            }

            Chunk[] chunks = zone.zones();
            if (chunks.length == 0) {
                InfoUtils.infoMsg(InfoLevel.WARN, player, "해당 사냥터의 구역이 아직 설정되지 않았습니다.");
                return;
            }
            InfoUtils.infoMsg(InfoLevel.WARN, player, "로드된 청크에 한하여 출력됩니다.");
            Arrays.stream(chunks).forEach(c -> player.sendMessage(ColorUtils.chat(String.format("&6x&f : %d, &6z&f : %d", c.getX(), c.getZ()))));
        } else InfoUtils.infoMsg(InfoLevel.ERROR, player, "유효하지 않은 명령어입니다.");
    }
}
