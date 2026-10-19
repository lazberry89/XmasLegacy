package org.lazberry.xmaslegacy.airdrop.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.airdrop.AirdropManager;
import org.lazberry.xmaslegacy.airdrop.supply.AirdropSpawnTask;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Commands(command = "supply")
@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class AirdropCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> commands = new HashMap<>();

    @Inject
    public AirdropCommand(AirdropManager airdropManager, AirdropSpawnTask spawnTask) {
        commands.put("drop", new AirdropCommandDrop(airdropManager));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) {
            InfoUtils.error(p, "명령어를 실행할 수 없습니다.");
            return true;
        }
        if (args.length < 1) {
            InfoUtils.error(p, "인자가 부족합니다.");
            return true;
        }
        var subcommand = commands.get(args[0].toLowerCase());
        if (subcommand == null) {
            InfoUtils.error(p, "유효한 명령어가 아닙니다. '{}' <-- ", args[0]);
            return true;
        }
        subcommand.execute(p, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
