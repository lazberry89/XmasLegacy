package org.lazberry.xmaslegacy.rank.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.rank.RankType;
import org.lazberry.xmaslegacy.rank.board.RankBoardManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.ParseEnum;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record BoardCommandRemove(RankBoardManager boardManager) implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 2) {
            RankType type = ParseEnum.of(RankType.class).parse(args[1]);
            if (type == null) {
                InfoUtils.error(player, "랭킹 타입이 잘못되었습니다.");
                return;
            }
            boolean result = boardManager.removeRankBoard(type);
            if (result) InfoUtils.info(player, "성공적으로 삭제했습니다.");
            else InfoUtils.error(player, "랭킹보드가 존재하지 않습니다.");
        }
    }
}
