package org.lazberry.xmaslegacy.RoleSwitch;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Commands(command = "book")
@Registry.Include(type = ServerType.MAIN)
public class BookCommand implements CommandExecutor {
    private final @NotNull MagicBook mb;

	@Inject
    public BookCommand(@NotNull MagicBook mb) {
	    this.mb = mb;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) {
            InfoUtils.error(p, "관리자용 명령어에요!");
            return true;
        }

        Block targetLoc = p.getTargetBlockExact(10);
        if (targetLoc == null) {
            InfoUtils.error(p, "타겟된 블록에 없어요. 블록을 보고 사용해주세요!");
            return true;
        }
        Location loc = targetLoc.getLocation();
        if (!mb.exists()) {
            mb.spawn(loc);
            InfoUtils.info(p, "직업책이 생성되었어요.");
        } else {
            InfoUtils.error(p, "이미 생성되어 있어요! 제거 후 사용해주세요.");
            Component delStand = ColorUtils.chat("&c&l[삭제하기]")
                    .clickEvent(ClickEvent.runCommand("/delstand"));
            Component msg = ColorUtils.chat(Alert.YELLOW + " 제거할까요? ").append(delStand);
            p.sendMessage(msg);
        }
        return true;
    }
}
