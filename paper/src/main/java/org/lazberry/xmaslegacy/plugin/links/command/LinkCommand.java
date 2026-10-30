package org.lazberry.xmaslegacy.plugin.links.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.plugin.links.ServerOfficialLinks;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.List;

@Commands(command = "links")
@Registry.Include(type = ServerType.GLOBAL)
public class LinkCommand implements CommandExecutor, TabCompleter {
    private final ServerOfficialLinks serverLinks;

    @Inject
    public LinkCommand(ServerOfficialLinks serverLinks) {
        this.serverLinks = serverLinks;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) {
            InfoUtils.error(p, "권한이 없습니다.");
            return true;
        }

        if (args.length >= 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                serverLinks.reload();
                InfoUtils.info(p, "링크 정보를 리로드했습니다.");
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return List.of("reload");
        return List.of();
    }
}
