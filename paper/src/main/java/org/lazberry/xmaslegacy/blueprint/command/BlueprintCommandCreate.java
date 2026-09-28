package org.lazberry.xmaslegacy.blueprint.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.blueprint.BluePrintManager;
import org.lazberry.xmaslegacy.blueprint.selection.BlueprintSelectionManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.Arrays;
import java.util.stream.Collectors;

public record BlueprintCommandCreate(BluePrintManager manager, BlueprintSelectionManager selectionManager) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (!player.isOp()) {
			InfoUtils.error(player, "명령어를 사용할 권한이 없습니다.");
			return;
		}
		//blueprint create id
		if (args.length < 2) {
			InfoUtils.error(player, "올바르지 않은 명령어입니다.");
			return;
		}
		String id = Arrays.stream(args)
				.skip(1)
				.filter(s -> !s.isBlank())
				.collect(Collectors.joining("_"));

		var uuid = player.getUniqueId();
		var optionalFirst = selectionManager.getFirstSelection(uuid);
		var optionalSecond = selectionManager.getSecondSelection(uuid);

		if (optionalFirst.isPresent() && optionalSecond.isPresent()) {
			var first = optionalFirst.get();
			var second = optionalSecond.get();

			var current = player.getLocation();

			manager.createAndSave(id, current, first, second).whenComplete((bluePrint, ex) -> {
				if (bluePrint != null) {
					InfoUtils.info(player, "성공적으로 도면을 완성했습니다.");
					InventoryHelper.giveItemOrDrop(player, bluePrint.getItem());
				} else if (ex != null) {
					InfoUtils.error(player, "도면 생성에 실패하였습니다.(이미 존재하거나 저장에 실패했습니다.)", ex);
				}
			});


		} else {
			if (optionalFirst.isEmpty()) InfoUtils.error(player, "첫번째 위치가 설정되지 않았습니다.");
			if (optionalSecond.isEmpty()) InfoUtils.error(player, "두번째 위치가 설정되지 않았습니다.");
		}
	}
}
