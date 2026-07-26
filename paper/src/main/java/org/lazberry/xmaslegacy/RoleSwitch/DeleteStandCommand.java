package org.lazberry.xmaslegacy.RoleSwitch;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Commands(command = "delstand")
@Registry.Exclude(type = ServerType.LOBBY)
public class DeleteStandCommand implements CommandExecutor {
    private final @NotNull MagicBook mb;

	@Inject
    public DeleteStandCommand(@NotNull MagicBook mb) {
	    this.mb = mb;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) {
            InfoUtils.error(p, "관리자용 명령어에요!");
            return true;
        }
        if (mb.getStand() == null) {
            InfoUtils.error(p, "현재 직업책이 없어요!");
        } else {
            mb.deleteStand();
            InfoUtils.info(p, "삭제되었습니다.");
        }
        return true;
    }
}
