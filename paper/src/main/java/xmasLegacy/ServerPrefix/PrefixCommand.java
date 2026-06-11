package xmasLegacy.ServerPrefix;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.MissionPrefix;
import org.lazberry.xmaslegacy.settings.RoleMastery;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.settings.Tier;
import xmasLegacy.Commands;
import xmasLegacy.InfoLevel;
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
@Commands(command = "prefix")
public class PrefixCommand implements CommandExecutor, TabCompleter {
    private final XmasLegacy plugin;
    private final PrefixManager PFM;
    private final UserManager UM;

    public PrefixCommand() {
        this.plugin = XmasLegacy.getInstance();
        this.PFM = PrefixManager.getInstance();
        this.UM = UserManager.getInstance();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull... args) {
        if (!(sender instanceof Player p)) return true;
        // /prefix <grant | deprive | equip | unequip | list | inv | inventory> <Player> <ServerPrefixType> <Prefix>
        if (p.isOp()) {
            if (args.length == 4) {
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    plugin.infoMsg(InfoLevel.ERROR, p, "해당 플레이어를 찾을 수 없습니다!");
                    return true;
                }
                var user = UM.getUser(target.getUniqueId());
                if (user == null) {
                    plugin.infoMsg(InfoLevel.ERROR, p, "해당 플레이어를 찾을 수 없습니다!");
                    return true;
                }
                ServerPrefix prefix = null;
                switch (args[2]) {
                    case "tier" -> {
                        try {
                            prefix = Tier.valueOf(args[3]);
                        } catch (IllegalArgumentException e) {
                            plugin.infoMsg(InfoLevel.ERROR, p, "칭호를 찾을 수 없습니다 : " + args[3]);
                        }
                    }
                    case "mastery" -> {
                        try {
                            prefix = RoleMastery.valueOf(args[3]);
                        } catch (IllegalArgumentException e) {
                            plugin.infoMsg(InfoLevel.ERROR, p, "칭호를 찾을 수 없습니다 : " + args[3]);
                        }
                    }
                    case "mission" -> {
                        try {
                            prefix = MissionPrefix.valueOf(args[3]);
                        } catch (IllegalArgumentException e) {
                            plugin.infoMsg(InfoLevel.ERROR, p, "칭호를 찾을 수 없습니다 : " + args[3]);
                        }
                    }
                    default -> {
                        plugin.infoMsg(InfoLevel.ERROR, p, "칭호 타입이 잘못되었습니다!");
                        return true;
                    }
                }
                if (prefix == null) return true;
                switch (args[0].toLowerCase()) {
                    case "grant" -> {
                        if (PFM.addPrefix(target, prefix)) {
                            plugin.infoMsg(InfoLevel.INFO, p, String.format("%s님에게 칭호 '%s'를 추가했습니다", target.getName(), prefix.name()));
                            plugin.infoMsg(InfoLevel.INFO, target, "칭호 '" + prefix.name() + "'가 부여되었습니다.");
                        } else {
                            plugin.infoMsg(InfoLevel.INFO, p, "이미 해당 칭호가 유저에게 존재합니다.");
                        }
                    }
                    case "deprive" -> {
                        if (PFM.removePrefix(target, prefix)) {
                            plugin.infoMsg(InfoLevel.INFO, p, String.format("%s님에게서 칭호 '%s'를 제거했습니다", target.getName(), prefix.name()));
                        } else {
                            plugin.infoMsg(InfoLevel.WARN, p, "해당 칭호를 보유하고 있지 않아요!");
                        }
                    }
                    case "equip" -> {
                        if (PFM.equipPrefix(target, prefix)) {
                            plugin.infoMsg(InfoLevel.INFO, p, String.format("칭호 '%s'를 %s님에게 장착했습니다.", prefix.name(), target.getName()));
                        } else {
                            plugin.infoMsg(InfoLevel.WARN, p, "유저가 해당 칭호를 보유하고 있지 않습니다.");
                        }
                    }
                    case "unequip" -> {
                        if (PFM.unequipPrefix(target)) {
                            plugin.infoMsg(InfoLevel.WARN, p, "유저의 칭호를 해제했습니다.");
                        } else {
                            plugin.infoMsg(InfoLevel.WARN, p, "유저가 칭호를 장착하고 있지 않습니다.");
                        }
                    }
                    default -> plugin.infoMsg(InfoLevel.ERROR, p, "잘못된 인자가 존재합니다.");
                }
            } else if (args.length == 1) {
                if (args[0].startsWith("inv")) {
                    p.openInventory(new PrefixInterface(p).getInventory());
                }
            } else if (args.length == 0) p.openInventory(new PrefixInterface(p).getInventory());
        } else {
            if (args.length == 1 && args[0].startsWith("inv") || args.length == 0) p.openInventory(new PrefixInterface(p).getInventory());
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = new ArrayList<>(List.of("inv", "inventory"));
            if (sender.isOp()) {
                commands.addAll(List.of("grant", "deprive", "equip", "unequip"));
            }
            StringUtil.copyPartialMatches(args[0], commands, completions);
            return completions;
        }

        if (!sender.isOp()) return List.of();

        if (args.length == 2) {
            return null;
        }

        if (args.length == 3) {
            org.bukkit.util.StringUtil.copyPartialMatches(args[2], List.of("tier", "mastery", "mission"), completions);
            return completions;
        }

        if (args.length == 4) {
            List<String> prefixNames = switch (args[2].toLowerCase()) {
                case "tier" -> Arrays.stream(Tier.values()).map(Tier::name).toList();
                case "mastery" -> Arrays.stream(RoleMastery.values()).map(RoleMastery::name).toList();
                case "mission" -> Arrays.stream(MissionPrefix.values()).map(MissionPrefix::name).toList();
                default -> List.of();
            };

            org.bukkit.util.StringUtil.copyPartialMatches(args[3], prefixNames, completions);
            return completions;
        }

        return List.of();
    }
}
