package org.lazberry.xmaslegacy.HuntingZone.ZoneCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.HuntingZone.HuntingZoneManager;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;

public class ZoneCommandDelete implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var hzm = HuntingZoneManager.INSTANCE;
        var chunk = player.getChunk();
        if (args.length >= 2) {
            var zone = hzm.getZone(args[1]);

            if (zone == null) {
                InfoUtils.error(player, "해당 사냥터가 설정되지 않았거나 적절하지 않습니다.");
                return;
            }
            if (zone.inZone(chunk)) {
                zone.shrink(chunk);
                InfoUtils.info(player, "해당 위치의 구역을 제외하였습니다.");
            } else {
                InfoUtils.warn(player, "해당 구역에 포함되지않은 청크입니다.");
            }
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
