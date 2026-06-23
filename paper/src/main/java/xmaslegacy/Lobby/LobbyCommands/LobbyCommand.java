package xmaslegacy.Lobby.LobbyCommands;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Lobby.LobbyManager;
import xmaslegacy.PluginUtils.ServerInitializer;
import xmaslegacy.PluginUtils.ServerType;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;
import xmaslegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LobbyCommand implements CommandExecutor, TabCompleter {
    private final @NotNull Map<String, SubCommand> sub = new HashMap<>();

    public LobbyCommand(@NotNull LobbyManager lbm) {
        this.sub.put("location", new LobbyCommandLocation(lbm));
        this.sub.put("reload", new LobbyCommandReload(lbm));
        this.sub.put("reset", new LobbyCommandReset(lbm));
        this.sub.put("set", new LobbyCommandSet(lbm));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 죄송한데, 관리자용 명령어에요!"));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return true;
        }
        if (!ServerInitializer.getServerType().equals(ServerType.LOBBY)) {
            p.sendMessage(ColorUtils.chat(Alert.RED + " 해당 서버에서는 사용할 수 없는 명령어에요!"));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return true;
        }
        if (args.length == 0) return true;
        var subCommand = sub.get(args[0].toLowerCase());
        if (subCommand == null) {
            InfoUtils.infoMsg(InfoLevel.ERROR, p, "유효한 명령어 사용법이 아닙니다.");
            return true;
        }
        subCommand.execute(p, args);
        return true;
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.add("set");
            result.add("location");
            result.add("reset");
            result.add("reload");
        }
        return result;
    }
}
