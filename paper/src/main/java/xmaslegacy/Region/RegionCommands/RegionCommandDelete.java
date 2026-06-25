package xmaslegacy.Region.RegionCommands;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Region.Region;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

@Slf4j
public class RegionCommandDelete implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        try {
            var rm = RegionManager.INSTANCE;
            if (args.length < 2) {
                InfoUtils.error(player, "사용법: /구역 <ID> 삭제/delete");
                return;
            }

            Region region = rm.getRegion(args[0]);
            if (region != null) {

                if (!player.isOp() && !region.getOwner().equals(player.getUniqueId())) {
                    player.sendMessage(ColorUtils.chat(Alert.RED + " 권한이 없습니다!"));
                    return;
                }


                if (args[1].equals("삭제") || args[1].equalsIgnoreCase("delete")) {
                    rm.removeRegion(region);
                    player.sendMessage(ColorUtils.chat(Alert.YELLOW + " 구역을 삭제했습니다. &6ID: " + region.Id()));
                } else
                    InfoUtils.error(player, "유효하지 않은 명령어입니다.");
            } else player.sendMessage(ColorUtils.chat(Alert.RED + " 아이디가 잘못되었습니다!"));
        } catch (Exception e) {
            log.error("Exception occurred while executing Region Deleting Command.", e);
        }
    }
}
