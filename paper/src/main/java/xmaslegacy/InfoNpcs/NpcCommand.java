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
			AbstractNpc npc;
			try {
				NpcType type = NpcType.valueOf(args[0].toUpperCase());
				npc = ncm.getNpcInstance(type);
				npc.sendCaption(p);
			} catch (IllegalArgumentException e) {
				plugin.infoMsg(InfoLevel.ERROR, p, "등록되지 않은 가이드이거나 잘못된 명령어입니다.");
				return true;
			}
		} else return true;
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
