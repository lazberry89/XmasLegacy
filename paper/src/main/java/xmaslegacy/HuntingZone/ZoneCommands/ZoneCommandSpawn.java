package xmaslegacy.HuntingZone.ZoneCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.HuntingZone.HuntingZoneManager;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

public class ZoneCommandSpawn implements SubCommand {

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
                InfoUtils.warn(player, "이미 활성화되어 있습니다.");
            } else {
                InfoUtils.info(player, "활성화 하였습니다.");
                zone.enable();
            }
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
