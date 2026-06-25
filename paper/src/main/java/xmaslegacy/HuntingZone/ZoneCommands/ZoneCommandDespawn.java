package xmaslegacy.HuntingZone.ZoneCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.HuntingZone.HuntingZoneManager;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

public class ZoneCommandDespawn implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var hzm = HuntingZoneManager.INSTANCE;
        if (args.length >= 2) {
            var zone = hzm.getZone(args[1]);

            if (zone == null) {
                InfoUtils.error(player, "해당 사냥터가 설정되지 않았거나 적절하지 않습니다.");
                return;
            }

            if (zone.isEnabled()) {
                InfoUtils.info(player, "비활성화 하였습니다.");
                zone.disable();
            } else InfoUtils.warn(player, "이미 비활성화 상태입니다.");
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
