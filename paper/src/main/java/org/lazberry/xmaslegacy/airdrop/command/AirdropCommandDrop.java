package org.lazberry.xmaslegacy.airdrop.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.airdrop.AirdropManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record AirdropCommandDrop(AirdropManager airdropManager) implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 1) {
            var loc = player.getLocation();
            airdropManager.spawnAirDrop(loc);
            InfoUtils.info(player, "보급을 불렀습니다.");
        }
    }
}
