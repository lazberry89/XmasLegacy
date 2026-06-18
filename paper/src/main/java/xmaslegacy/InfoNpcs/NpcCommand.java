package xmaslegacy.InfoNpcs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
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
        if (!p.isOp()) {
            plugin.infoMsg(InfoLevel.ERROR, p, "관리자 전용 명령어입니다!");
            return true;
        }
        if (args.length == 3) {
            NpcType type;
            try {
                type = NpcType.valueOf(args[0].toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.infoMsg(InfoLevel.ERROR, p, "유효하지 않은 타입입니다.");
                return true;
            }
	        EntityType enType;
			try {
				enType = EntityType.valueOf(args[2]);
			} catch (IllegalArgumentException e) {
				plugin.infoMsg(InfoLevel.ERROR, p, "유효하지 않은 타입입니다.");
				return true;
			}
            if (args[1].equalsIgnoreCase("spawn"))
                ncm.getNpcInstance(type).spawn(p.getLocation(), enType);
        }
		if (args.length == 1 && args[0].equalsIgnoreCase("remove")) {
			Entity target = p.getTargetEntity(10, false);
			if (target == null) {
				plugin.infoMsg(InfoLevel.ERROR, p, "타겟이 없습니다.");
				return true;
			}
			var container = target.getPersistentDataContainer();
			if (container.has(AbstractNpc.key())) {
				target.remove();
				plugin.infoMsg(InfoLevel.INFO, p, "제거하였습니다.");
			}
		}
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
