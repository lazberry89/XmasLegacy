package org.lazberry.xmaslegacy.rank.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.rank.RankType;
import org.lazberry.xmaslegacy.rank.board.RankBoardManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Commands(command = "board")
@Registry.Exclude(type = ServerType.LOBBY)
public class BoardCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> commands = new HashMap<>();

    @Inject
    public BoardCommand(RankBoardManager boardManager) {
        commands.put("spawn", new BoardCommandSpawn(boardManager));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) {
            InfoUtils.error(p, "권한이 없습니다.");
            return true;
        }
        if (args.length < 1) {
            InfoUtils.error(p, "인자가 부족합니다.");
            return true;
        }
        var subcommand = commands.get(args[0].toLowerCase());
        if (subcommand != null) subcommand.execute(p, args);
        else InfoUtils.error(p, "올바른 명령어가 아닙니다.");

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return switch (args.length) {
            case 1 -> List.of("spawn", "remove");
            case 2 -> Stream.of(RankType.values()).map(RankType::name).toList();
            default -> List.of();
        };
    }
}
