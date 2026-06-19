package xmaslegacy.InfoNpcs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.XmasLegacy;

import java.util.List;

@Commands(command = "guide")
public class NpcCommand implements CommandExecutor, TabCompleter {
    private final @NotNull XmasLegacy plugin;
    private final @NotNull NpcManager ncm;

    public NpcCommand() {
        this.plugin = XmasLegacy.getInstance();
        this.ncm = NpcManager.INSTANCE;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		if (!p.isOp()) return true;
		if (args.length == 1) {
			switch (args[0].toLowerCase()) {
				case "main" -> {
					var main = ncm.getNpcInstance(NpcType.MAIN);
					if (main == null) {
						plugin.infoMsg(InfoLevel.ERROR, p, "등록되지 않은 가이드입니다.");
						return true;
					}
					main.sendCaption(p);
				}
				default -> {}
			}
		} else return true;
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
