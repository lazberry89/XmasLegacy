package xmasLegacy.RoleSwitch;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmasLegacy.InfoLevel;
import xmasLegacy.XmasLegacy;

public class DeleteStandCommand implements CommandExecutor {
    private final XmasLegacy plugin;
    private final MagicBook MB;

    public DeleteStandCommand() {
        this.plugin = XmasLegacy.getInstance();
        this.MB = MagicBook.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) {
            plugin.infoMsg(InfoLevel.ERROR, p, "관리자용 명령어에요!");
            return true;
        }
        if (MB.getStand() == null) {
            plugin.infoMsg(InfoLevel.ERROR, p, "현재 직업책이 없어요!");
        } else {
            MB.getStand().remove();
            plugin.infoMsg(InfoLevel.INFO, p, "삭제되었습니다.");
        }
        return true;
    }
}
