package xmaslegacy.RoleSwitch;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;

@Commands(command = "delstand")
public class DeleteStandCommand implements CommandExecutor {
    private final MagicBook MB;

    public DeleteStandCommand() {
        this.MB = MagicBook.INSTANCE;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) {
            InfoUtils.error(p, "관리자용 명령어에요!");
            return true;
        }
        if (MB.getStand() == null) {
            InfoUtils.error(p, "현재 직업책이 없어요!");
        } else {
            MB.deleteStand();
            InfoUtils.info(p, "삭제되었습니다.");
        }
        return true;
    }
}
