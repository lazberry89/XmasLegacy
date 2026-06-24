package xmaslegacy.Region.RegionCommands;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Region.Region;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

@Slf4j
public class RegionCommandSetting implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        try {
            if (args.length < 3) {
                InfoUtils.error(player, "사용법: /구역 <ID> <설정> <값>");
                return;
            }

            var rm = RegionManager.INSTANCE;
            Region region = rm.getRegion(args[0]);
            if (region == null) {
                player.sendMessage(ColorUtils.chat(Alert.RED + " 아이디가 잘못되었습니다!"));
                return;
            }

            if (!player.isOp() && !region.getOwner().equals(player.getUniqueId())) {
                player.sendMessage(ColorUtils.chat(Alert.RED + " 권한이 없습니다!"));
                return;
            }

            String settingType = args[1].toLowerCase();
            boolean isAllow = args[2].equals("허용") || args[2].equalsIgnoreCase("allow");
            boolean isDeny = args[2].equals("차단") || args[2].equalsIgnoreCase("deny");

            switch (settingType) {
                case "interaction", "상호작용" -> {
                    if (isAllow) {
                        region.allowInteraction();
                        player.sendMessage(ColorUtils.chat(Alert.GREEN + " 상호작용을 허용했습니다."));
                    } else if (isDeny) {
                        region.blockInteraction();
                        player.sendMessage(ColorUtils.chat(Alert.YELLOW + " 상호작용을 차단했습니다."));
                    } else InfoUtils.error(player, "유효하지 않은 값입니다.");
                }
                case "entry", "입장" -> {
                    if (isAllow) {
                        region.allowEntry();
                        player.sendMessage(ColorUtils.chat(Alert.GREEN + " 입장을 허용했습니다."));
                    } else if (isDeny) {
                        region.blockEntry();
                        player.sendMessage(ColorUtils.chat(Alert.YELLOW + " 입장을 차단했습니다."));
                    } else InfoUtils.error(player, "유효하지 않은 값입니다.");
                }
                default -> InfoUtils.error(player, "유효하지 않은 설정 종류입니다. (상호작용/입장)");
            }
        } catch (Exception e) {
            log.error("Exception occurred while executing Region Setting Command.", e);
        }
    }
}
