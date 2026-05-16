package xmasLegacy.ServerPrefix;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.ServerPrefix;
import org.lazberry.xmaslegacy.settings.Tier;
import xmasLegacy.InfoLevel;
import xmasLegacy.XmasLegacy;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class PrefixCommand implements CommandExecutor, TabCompleter {
    private final XmasLegacy plugin;
    private final PrefixManager PFM;
    private final UserManager UM;

    public PrefixCommand(XmasLegacy plugin) {
        this.plugin = plugin;
        this.PFM = plugin.PFM;
        this.UM = plugin.UM;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull... args) {
        if (!(sender instanceof Player p)) return true;
        // /prefix <grant | deprive | equip | unequip | list | inv> <Player> <ServerPrefixType> <Prefix>
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
                ServerPrefix prefix;
                switch (args[2]) {
                    case "tier" -> {
                        try {
                            prefix = Tier.valueOf(args[3]);
                        }
                    }
                    case "mastery" -> {}
                    case "mission" -> {}
                    default -> {}
                }
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
