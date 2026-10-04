package org.lazberry.xmaslegacy.shop.command;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.shop.showcase.ShowCaseManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record GoodsCommandShowcase(ShowCaseManager showCaseManager) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length < 3) {
			InfoUtils.error(player, "올바르지 않는 명령어입니다.");
			return;
		}
		String name = args[1];
		String action = args[2].toLowerCase();
		RayTraceResult result = player.rayTraceBlocks(10);

		if (result != null && result.getHitBlock() != null) {
			Block block = result.getHitBlock();
			BlockFace face = result.getHitBlockFace();
			Location loc = block.getLocation();

			switch (action) {
				case "create" -> {
					if (showCaseManager.spawnShowCase(name, loc, face))
						InfoUtils.info(player, "케이스를 생성했습니다.");
					else InfoUtils.error(player, "케이스를 생성하지 못했습니다.");
				}
				case "remove" -> {
					if (showCaseManager.removeShowCase(name))
						InfoUtils.info(player, "성공적으로 제거했습니다.");
					else InfoUtils.error(player, "제거하지 못했습니다.");
				}
				default -> InfoUtils.error(player, "올바르지 않는 명령어입니다.");
			}
		} else InfoUtils.error(player, "케이스를 설치할 블록을 바라보고 사용해주세요!");
	}
}
