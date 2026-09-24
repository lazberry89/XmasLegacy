package org.lazberry.xmaslegacy.blueprint.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.blueprint.BluePrintManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.Arrays;
import java.util.stream.Collectors;

public record BlueprintCommandGive(BluePrintManager manager) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (!player.isOp()) {
			InfoUtils.error(player, "명령어를 실행할 권한이 없습니다.");
			return;
		}
		if (args.length < 2) {
			InfoUtils.error(player, "올바르지 않은 명령어입니다.");
			return;
		}
		String id = Arrays.stream(args)
				.skip(1)
				.filter(s -> !s.isBlank())
				.collect(Collectors.joining("_"));
		var optional = manager.getBluePrint(id);
		if (optional.isPresent()) {
			InventoryHelper.giveItemOrDrop(player, optional.get().getItem());
			InfoUtils.info(player, "도면이 지급되었습니다.");
		} else InfoUtils.error(player, "존재하지 않는 도면 이름입니다.");
	}
}
