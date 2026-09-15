package org.lazberry.xmaslegacy.casino.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.List;
import java.util.Map;

@Commands(command = "casino")
@Registry.Include(type = ServerType.MAIN)
public class CasinoCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> commands;
    private final XmasLegacy plugin;

    @Inject
    public CasinoCommand(XmasLegacy plugin) {
        this.plugin = plugin;
        this.commands = Map.of(
                "shop", new CasinoCommandShop(plugin)
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) {
            InfoUtils.error(p, "You don't have permission to use this command.");
            return true;
        }
        if (args.length < 1) {
            InfoUtils.error(p, "Not a valid command.");
            return true;
        }
        SubCommand sub = commands.get(args[0].toLowerCase());
        if (sub == null) {
            InfoUtils.error(p, "Not a valid command.");
            return true;
        }
        sub.execute(p, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
