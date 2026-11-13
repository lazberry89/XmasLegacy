package org.lazberry.xmaslegacy.party.tpa;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Commands;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.InfoUtils;

/** /tpa &lt;player&gt;: asks a player anywhere on the network to let you teleport to them. */
@Commands(command = "tpa")
@Components.Exclude(type = ServerType.LOBBY)
public class TpaCommand implements CommandExecutor {
    private final TpaManager tpaManager;

    @Inject
    public TpaCommand(TpaManager tpaManager) {
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return false;
        if (args.length < 1) {
            InfoUtils.error(p, "사용법: /tpa <플레이어>");
            return true;
        }

        if (p.getName().equalsIgnoreCase(args[0])) {
            InfoUtils.error(p, "자기 자신에게는 요청할 수 없습니다.");
            return true;
        }

        // Same name -> uuid lookup the whisper command uses, so targets on other servers resolve too.
        var targetUuid = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
        if (tpaManager.sendTpaRequest(p.getUniqueId(), p.getName(), targetUuid)) {
            InfoUtils.info(p, "&6{}&f님에게 텔레포트 요청을 보냈습니다. (60초간 유효)", args[0]);
        } else {
            InfoUtils.error(p, "해당 유저가 오프라인입니다.");
        }
        return true;
    }
}
