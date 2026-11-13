package org.lazberry.xmaslegacy.party.tpa;

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

/** /tpaccept: accepts the teleport request waiting for you. */
@Commands(command = "tpaccept")
@Components.Exclude(type = ServerType.LOBBY)
public class TpacceptCommand implements CommandExecutor {
    private final TpaManager tpaManager;

    @Inject
    public TpacceptCommand(TpaManager tpaManager) {
        this.tpaManager = tpaManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return false;

        if (tpaManager.acceptTpaRequest(p)) InfoUtils.info(p, "텔레포트 요청을 수락했습니다.");
        else InfoUtils.error(p, "받은 요청이 없거나, 요청한 유저가 오프라인입니다.");
        return true;
    }
}
