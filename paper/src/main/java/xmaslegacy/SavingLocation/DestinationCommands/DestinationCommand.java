package xmaslegacy.SavingLocation.DestinationCommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.SavingLocation.DestinationType;
import xmaslegacy.SavingLocation.SpawnRepository;
import xmaslegacy.Utils.InfoUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Commands(command = "destination")
public final class DestinationCommand implements CommandExecutor, TabCompleter {

    public DestinationCommand() {}

    ///destination list/set/move/reset/reload/save/location/loc <DestinationType>

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) {
            InfoUtils.error(p, "관리자용 명령어에요!");
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            new DestinationCommandList().execute(p, args);
            return true;
        }
        if (args.length < 2) {
            InfoUtils.error(p, "올바르지 않은 명령어입니다.");
            return true;
        }
        DestinationType value;
        try {
            value = DestinationType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            InfoUtils.error(p, String.format("올바른 타입이 아닙니다. (%s)", String.join(",",
                    Arrays.stream(DestinationType.values())
                            .filter(v -> v != DestinationType.LOBBY)
                            .map(Objects::toString)
                            .collect(java.util.stream.Collectors.joining(",")))));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ENGLISH)) {
            case "set" -> new DestinationCommandSet(value).execute(p, args);
            case "move" -> new DestinationCommandMove(value).execute(p, args);
			case "reset" -> new DestinationCommandReset(value).execute(p, args);
			case "reload" -> new DestinationCommandReload(value).execute(p, args);
			case "location", "loc" -> new DestinationCommandLocation(value).execute(p, args);
        }
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return List.of("list", "set", "move", "reset", "reload", "save", "location", "loc");
        return Arrays.stream(SpawnRepository.INSTANCE.availableTypes())
                .map(Objects::toString)
                .map(String::toLowerCase)
                .toList();
    }
}