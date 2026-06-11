package xmasLegacy;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Commands(command = "vanish")
public class GhostCommand implements CommandExecutor {
    private final GhostModeManager GMM;

    public  GhostCommand() {
        this.GMM = GhostModeManager.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player p)) return true;
        GMM.toggle(p);
        return true;
    }
}
