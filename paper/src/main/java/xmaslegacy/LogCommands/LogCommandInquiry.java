package xmaslegacy.LogCommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Inquiry.InquiryManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;
import xmaslegacy.XmasLegacy;

import java.util.List;
import java.util.UUID;

public class LogCommandInquiry implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 2) {
            String targetName = args[1];
            player.sendMessage(ColorUtils.chat("&7로그를 불러오는 중입니다..."));

            var plugin = XmasLegacy.getInstance();
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                OfflinePlayer op = Bukkit.getOfflinePlayer(targetName);
                UUID uuid = op.getUniqueId();

                List<String> logs = InquiryManager.INSTANCE.getInquiryLogs(uuid);

                if (logs.isEmpty() || (logs.size() == 1 && logs.getFirst().contains("없습니다")))
                    player.sendMessage(ColorUtils.chat(Alert.RED + "'" + targetName + "' 유저의 기록이 없습니다."));
                else logs.forEach(player::sendMessage);
            });
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
