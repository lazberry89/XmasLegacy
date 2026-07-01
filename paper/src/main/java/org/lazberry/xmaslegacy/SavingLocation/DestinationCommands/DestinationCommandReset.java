package org.lazberry.xmaslegacy.SavingLocation.DestinationCommands;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.SavingLocation.DestinationType;
import org.lazberry.xmaslegacy.SavingLocation.SpawnRepository;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;

@Slf4j
public record DestinationCommandReset(@NotNull DestinationType type) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		var value = SpawnRepository.INSTANCE.get(type);
		value.resetSpawn();
		InfoUtils.info(player, "위치가 초기화되었습니다.");
		log.warn("{}'s spawn has been reset.", type);
	}
}
