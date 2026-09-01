package org.lazberry.xmaslegacy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ServerTransfer;

@Commands(command = "wild")
@Registry.Include(type = ServerType.GLOBAL)
public class WildTestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) return true;
        ServerTransfer.transfer(ServerType.WILD, p);
        return true;
    }
}
