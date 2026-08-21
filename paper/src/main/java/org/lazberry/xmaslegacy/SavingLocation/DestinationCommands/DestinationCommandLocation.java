package org.lazberry.xmaslegacy.SavingLocation.DestinationCommands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.SavingLocation.DestinationType;
import org.lazberry.xmaslegacy.SavingLocation.SpawnRepository;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public record DestinationCommandLocation(@NotNull DestinationType type, @NotNull SpawnRepository spawnRepo) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		var value = spawnRepo.get(type);
		Location loc = value.getSpawn();

		if (loc == null) {
			InfoUtils.error(player, "위치정보가 설정되지 않았거나 불러올 수 없습니다!");
			return;
		}

		InfoUtils.info(player, value.formattedLocation());
	}
}
