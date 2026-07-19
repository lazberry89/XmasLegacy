package org.lazberry.xmaslegacy.GhostMode;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.PluginUtils.Initializer.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;

@Commands(command = "vanish")
public class GhostCommand implements CommandExecutor {
    private final @NotNull GhostModeManager gmm;

	@Inject
    public GhostCommand(@NotNull GhostModeManager gmm) {
		this.gmm = gmm;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(commandSender instanceof Player p)) return true;
        gmm.toggle(p);
        return true;
    }
}
