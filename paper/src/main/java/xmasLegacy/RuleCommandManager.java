package xmasLegacy;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Prefix;
import org.lazberry.xmaslegacy.RuleManager;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class RuleCommandManager implements CommandExecutor, TabCompleter {
    private final RuleManager RM;

    public RuleCommandManager(RuleManager RM) {
        this.RM = RM;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
        if (!(commandSender instanceof Player p)) return false;
        if (!p.isOp()) {
            p.sendMessage(ColorUtils.chat(STR."\{Prefix.RED} 관리자 전용입니다. 관리자에게 문의하세요!"));
            p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
            return true;
        }

        if (args.length == 0) {
            p.sendMessage(ColorUtils.chat("&c&l차단&f&l 단어 리스트: "));
            for (String bw : RM.getBadWordList()) {
                p.sendMessage(ColorUtils.chat(STR."&c&l-&f \{bw}"));
            }
        }
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "append" -> {
                    if (RM.getBadWordList().contains(args[1])) {
                        p.sendMessage(ColorUtils.chat(STR."\{Prefix.RED} 이미 존재하는 항목입니다."));
                        p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
                    } else {
                        RM.addBadWordList(args[1]);
                        p.sendMessage(ColorUtils.chat(STR."\{Prefix.YELLOW} 추가되었습니다. /filter list로 확인"));
                        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.0f);
                    }
                }
                case "remove" -> {
                    if (RM.getBadWordList().contains(args[1])) {
                        RM.removeBadWordList(args[1]);
                        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.0f);
                    } else {
                        p.sendMessage(ColorUtils.chat(STR."\{Prefix.RED} 존재하지 않는 항목입니다."));
                        p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String... args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("append");
            completions.add("remove");
            completions.add("list");

            return completions.stream()
                    .filter(str -> str.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove")) {
                return RM.getBadWordList().stream()
                        .filter(word -> word.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }

            if (args[0].equalsIgnoreCase("append")) {
                completions.add("<단어>");
                return completions;
            }
        }

        return new ArrayList<>();
    }
}
