package xmaslegacy.RuleCommands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RuleManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Commands(command = "filter")
public class RuleCommand implements CommandExecutor, TabCompleter {
    private final @NotNull RuleManager rm;
    private final @NotNull Map<String, SubCommand> subCommands = new HashMap<>(4);

    public RuleCommand() {
        this.rm = RuleManager.INSTANCE;
        this.subCommands.put("add", new RuleCommandAdd());
        this.subCommands.put("remove", new RuleCommandRemove());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull ...args) {
        if (!(commandSender instanceof Player p)) return false;
        if (!p.isOp()) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 관리자 전용입니다. 관리자에게 문의하세요!"));
            p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
            return true;
        }

        if (args.length == 0 || args.length == 1 && args[0].equalsIgnoreCase("list")) {
            p.sendMessage(ColorUtils.chat("&c&l차단&f&l 단어 리스트: "));
            for (String bw : rm.getBadWordList()) {
                p.sendMessage(ColorUtils.chat(String.format("&c&l-&f %s", bw)));
            }
            return true;
        }

        SubCommand sub = this.subCommands.get(args[0].toLowerCase());

        if (sub != null) {
            sub.execute(p, args);
            return true;
        }
        InfoUtils.infoMsg(InfoLevel.ERROR, p, "유효하지 않은 명령어입니다.");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull ...args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("add");
            completions.add("remove");
            completions.add("list");

            return completions.stream()
                    .filter(str -> str.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                return rm.getBadWordList().stream()
                        .filter(word -> word.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }

            if (args[0].equalsIgnoreCase("add")) {
                completions.add("<단어>");
                return completions;
            }
        }

        return new ArrayList<>();
    }
}
