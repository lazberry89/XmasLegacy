package org.lazberry.xmaslegacy.blueprint.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.blueprint.BluePrintManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.Arrays;
import java.util.stream.Collectors;

public record BlueprintCommandRemove(BluePrintManager blueprintManager) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (!player.isOp()) {
			InfoUtils.error(player, "명령어를 사용할 권한이 없습니다.");
			return;
		}
		if (args.length < 2) {
			InfoUtils.error(player, "올바르지 않은 명령어 사용법입니다.");
			return;
		}
		String id = Arrays.stream(args)
				.skip(1)
				.filter(s -> !s.isBlank())
				.collect(Collectors.joining("_"));
		if (blueprintManager.removeBlueprint(id)) InfoUtils.info(player, "성공적으로 도면을 삭제했습니다: {}", id);
		else InfoUtils.error(player, "존재하지 않는 도면입니다. ({})", id);
	}
}
