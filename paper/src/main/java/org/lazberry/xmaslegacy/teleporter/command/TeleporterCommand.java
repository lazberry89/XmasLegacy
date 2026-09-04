package org.lazberry.xmaslegacy.teleporter.command;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.teleporter.logic.TeleporterCreateManager;
import org.lazberry.xmaslegacy.teleporter.logic.TeleporterManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Commands(command = "portal")
@Registry.Include(type = ServerType.GLOBAL)
public class TeleporterCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> commands;
    private final TeleporterCreateManager tcm;
    private final TeleporterManager tm;

    @Inject
    public TeleporterCommand(TeleporterCreateManager tcm, TeleporterManager tm) {
        this.tcm = tcm;
        this.tm = tm;
        this.commands = Map.of(
                "create", new TeleporterCommandCreate(tm, tcm)
        );
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {
            log.error("Only Player could execute a command.");
            return true;
        }
        if (!p.isOp()) {
            InfoUtils.error(p, "You don't have permission to use this command!");
            return true;
        }
        if (args.length < 1) {
            p.getInventory().addItem(tm.tool());
            return true;
        }
        Optional.ofNullable(commands.get(args[0].toLowerCase()))
                .ifPresentOrElse(c -> c.execute(p, args), () -> InfoUtils.error(p, "No such command found."));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return List.of("create", "remove");
        if (args.length >= 2) return tm.identityList();
        return Collections.emptyList();
    }
}
