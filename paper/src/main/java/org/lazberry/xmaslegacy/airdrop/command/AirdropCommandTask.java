package org.lazberry.xmaslegacy.airdrop.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.airdrop.supply.AirdropSpawnManager;
import org.lazberry.xmaslegacy.airdrop.supply.AirdropSpawnTask;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record AirdropCommandTask(AirdropSpawnManager spawnManager, AirdropSpawnTask spawnTask) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		if (args.length >= 2) {
			String key = args[1];
			if (key.equalsIgnoreCase("center")) {
				spawnManager.setCenter(player.getLocation());
				InfoUtils.info(player, "현재 위치를 중앙으로 설정했습니다.");
				return;
			}


			boolean value = args[1].equalsIgnoreCase("on");
			if (value) {
				spawnTask.startTask(XmasLegacy.getInstance());
				InfoUtils.info(player, "보급 필드드랍을 시작했습니다.");
			} else {
				spawnTask.stopTask();
				InfoUtils.warn(player, "보급 필드드랍이 정지됐습니다.");
			}
		}
	}
}
