package org.lazberry.xmaslegacy.magic;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.framework.ServerType;

@Commands(command = "delstand")
@Registry.Include(type = ServerType.MAIN)
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
		mb.remove();
		InfoUtils.info(p, "삭제되었습니다.");
        return true;
    }
}
