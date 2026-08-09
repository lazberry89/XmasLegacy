package org.lazberry.xmaslegacy.stock.commands.admin;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;
import org.lazberry.xmaslegacy.stock.StockManager;

import java.util.HashMap;
import java.util.Map;

public class StockCommandAdmin implements SubCommand {
    private final Map<String, SubCommand> commands = new HashMap<>();
    private final StockManager sm;

    public StockCommandAdmin(StockManager sm) {
        this.sm = sm;
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 2) {
            var str = args[1];
            var command = commands.get(str);
            if (command == null) {
                InfoUtils.error(player, "존재하지 않는 명령어입니다.");
                return;
            }
            command.execute(player, args);
        }
    }
}
