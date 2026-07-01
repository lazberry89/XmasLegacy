package org.lazberry.xmaslegacy.HuntingZone.ZoneCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.HuntingZone.HuntingZoneManager;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;

public class ZoneCommandAlive implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var hzm = HuntingZoneManager.INSTANCE;
        if (args.length >= 2) {
            var zone = hzm.getZone(args[1]);

            if (zone == null) {
                InfoUtils.error(player, "해당 사냥터가 설정되지 않았거나 적절하지 않습니다.");
                return;
            }
            InfoUtils.warn(player, String.format("현재 사냥터 몹 수(최대 마릿수 초과시 검사필요): %d/%d", zone.getAliveMobCount(), zone.getMaxSpawn()));
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
