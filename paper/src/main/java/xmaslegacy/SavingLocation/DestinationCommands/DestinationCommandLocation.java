package xmaslegacy.SavingLocation.DestinationCommands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.SavingLocation.DestinationType;
import xmaslegacy.SavingLocation.SpawnRepository;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

public record DestinationCommandLocation(@NotNull DestinationType type) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		var value = SpawnRepository.INSTANCE.get(type);
		Location loc = value.getSpawn();

		if (loc == null) {
			InfoUtils.error(player, "위치정보가 설정되지 않았거나 불러올 수 없습니다!");
			return;
		}

		InfoUtils.info(player, String.format("world : %s, x: %.1f, y: %.1f, z: %.1f, yaw: %.1f, pitch: %.1f",
				loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
	}
}
