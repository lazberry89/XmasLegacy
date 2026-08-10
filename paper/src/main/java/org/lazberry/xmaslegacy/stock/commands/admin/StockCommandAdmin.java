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

    public StockCommandAdmin(StockManager sm) {
		var add = new StockCommandAdd(sm);
		var remove = new StockCommandRemove(sm);
		var update = new StockCommandUpdate(sm);
        commands.put("add", add);
		commands.put("추가", add);
		commands.put("remove", remove);
		commands.put("제거", remove);
		commands.put("update", update);
		commands.put("업데이트", update);
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (!player.isOp()) {
			InfoUtils.error(player, "You don't have permission to use this command.");
			return;
		}
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
