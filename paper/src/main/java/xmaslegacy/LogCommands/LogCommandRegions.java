package xmaslegacy.LogCommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Region.Region;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

import java.util.List;

public class LogCommandRegions implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var rm = RegionManager.INSTANCE;
        if (args.length >= 2) {
            OfflinePlayer of = Bukkit.getOfflinePlayer(args[1]);
            if (of.hasPlayedBefore()) {
                List<Region> regions = rm.getRegion(of.getUniqueId());
                if (regions.isEmpty()) {
                    player.sendMessage(ColorUtils.chat(Alert.RED + " 구역이 없습니다."));
                    return;
                }
                rm.sendRegionFormat(player, regions);
            } else {
                player.sendMessage(ColorUtils.chat(Alert.RED + " 유저가 존재하지 않습니다."));
                player.playSound(player, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
            }
        } else if (args.length == 1) {
            List<Region> regions = rm.getRegions();
            if (regions.isEmpty()) {
                player.sendMessage(ColorUtils.chat(Alert.RED + " 구역이 없습니다."));
                return;
            }
            rm.sendRegionFormat(player, regions);
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
