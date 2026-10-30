package org.lazberry.xmaslegacy.chat.whisper.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.chat.whisper.WhisperManager;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.*;
import java.util.stream.Collectors;

@Commands(command = "whisper", aliases = {"귓", "w"})
@Registry.Include(type = ServerType.GLOBAL)
public class WhisperCommand implements CommandExecutor, TabCompleter {
    private final Map<String, SubCommand> commands = new HashMap<>();
    private final WhisperManager whisperManager;

    @Inject
    public WhisperCommand(WhisperManager whisperManager) {
        this.whisperManager = whisperManager;
        commands.put("mute", new WhisperCommandMute());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return false;
        if (args.length < 1) {
            InfoUtils.error(p, "올바르지 않는 명령어입니다.");
            return true;
        }
        var value = args[0].toLowerCase();
        var subCommand = commands.get(value);
        if (subCommand == null) {
            new WhisperCommandPlayer(whisperManager).execute(p, args);
            return true;
        }
        subCommand.execute(p, args);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return switch (args.length) {
            case 1 -> {
                var userUuids = JedisService.getAllProxyUsers();
                List<String> onlineNames = userUuids.stream()
                        .map(Bukkit::getOfflinePlayer)
                        .filter(OfflinePlayer::hasPlayedBefore)
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.toCollection(ArrayList::new));
                String lang = command.getName();
                if (lang.equalsIgnoreCase("whisper") || lang.equalsIgnoreCase("w")) {
                    onlineNames.add("mute");
                    onlineNames.add("unmute");
                } else {
                    onlineNames.add("차단");
                    onlineNames.add("허용");
                }
                yield onlineNames;
            }
            case 2 -> List.of("<text>");
            default -> List.of();
        };
    }
}
