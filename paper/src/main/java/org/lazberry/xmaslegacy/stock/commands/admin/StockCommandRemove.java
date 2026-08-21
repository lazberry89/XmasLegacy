package org.lazberry.xmaslegacy.stock.commands.admin;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;
import org.lazberry.xmaslegacy.stock.StockManager;

import java.util.Locale;

public record StockCommandRemove(StockManager sm) implements SubCommand {
    /// stock admin remove <이름>
    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 3) {
            var name = args[2].toLowerCase(Locale.ROOT);
            if (name.trim().isEmpty()) {
                InfoUtils.error(player, "올바른 주식 이름을 입력해주세요.");
                return;
            }
            if (sm.removeStock(name) == null) {
                InfoUtils.error(player, "존재하지 않는 주식입니다.");
                return;
            }
            InfoUtils.info(player, "성공적으로 제거했습니다.");
        }
    }
}
