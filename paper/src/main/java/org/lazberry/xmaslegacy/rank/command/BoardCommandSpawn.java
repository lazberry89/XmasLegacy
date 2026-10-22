package org.lazberry.xmaslegacy.rank.command;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.rank.RankType;
import org.lazberry.xmaslegacy.rank.board.RankBoardManager;
import org.lazberry.xmaslegacy.utils.Axiom;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.ParseEnum;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record BoardCommandSpawn(RankBoardManager boardManager) implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 3) {
            String value = args[1].toUpperCase();
            RankType type = ParseEnum.of(RankType.class).parse(value);
            if (type == null) {
                InfoUtils.error(player, "올바른 랭크 타입이 아닙니다. ('{}')", value);
                return;
            }
            int limit;
            try {
                limit = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                InfoUtils.error(player, "올바른 숫자를 입력해주세요. ('{}')", args[2]);
                return;
            }
            Location loc = Axiom.snapDegrees(player.getLocation());
            boardManager.createRankBoard(type, loc, limit);
            InfoUtils.info(player, "랭킹보드를 생성했습니다.");
        }
    }
}
