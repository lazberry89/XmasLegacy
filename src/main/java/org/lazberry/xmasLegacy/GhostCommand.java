package org.lazberry.xmasLegacy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ClassCanBeRecord")
public class GhostCommand implements CommandExecutor {
    private final GhostModeManager GMM;

    public  GhostCommand(GhostModeManager GMM) {
        this.GMM = GMM;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player p)) return true;
        GMM.toggle(p);
        return true;
    }
}
