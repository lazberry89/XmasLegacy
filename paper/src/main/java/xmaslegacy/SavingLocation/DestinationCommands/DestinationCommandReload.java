package xmaslegacy.SavingLocation.DestinationCommands;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.SavingLocation.DestinationType;
import xmaslegacy.SavingLocation.SpawnRepository;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

@Slf4j
public record DestinationCommandReload(@NotNull DestinationType type) implements SubCommand {

	@Override
	public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
		var value = SpawnRepository.INSTANCE.get(type);
		InfoUtils.warn(player, "&7리로드 중입니다..");
		value.reload().whenComplete((done, ex) -> {
			if (ex != null) {
				log.error("Failed to reload Spawn Location {}. {} {}", value, ex.getCause(), ex.getMessage());
				InfoUtils.error(player, "위치 로드에 실패했습니다!");
				return;
			}
			if (done == null) {
				InfoUtils.error(player, "위치 로드에 실패했습니다!");
				return;
			}
			if (done) InfoUtils.info(player, "리로드에 성공했습니다.");
			else InfoUtils.error(player, "위치 로드에 실패했습니다!");
		});
	}
}
