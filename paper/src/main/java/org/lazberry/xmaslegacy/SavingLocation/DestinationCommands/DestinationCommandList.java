package org.lazberry.xmaslegacy.SavingLocation.DestinationCommands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.SavingLocation.DestinationType;
import org.lazberry.xmaslegacy.SavingLocation.SpawnRepository;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.Arrays;

public class DestinationCommandList implements SubCommand {
	private final @NotNull SpawnRepository spawnRepo;

	public DestinationCommandList(@NotNull SpawnRepository spawnRepo) {
		this.spawnRepo = spawnRepo;
	}

	@Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        Arrays.stream(spawnRepo.availableTypes())
                .forEach(t -> sendFormatted(t, player));
    }

    private void sendFormatted(@NotNull DestinationType type, @NotNull Player p) {
        var value = spawnRepo.get(type);
        Location loc = value.getSpawn();
        p.sendMessage(ColorUtils.chat(loc == null ?
                String.format(
                """
                &5&l%s&f&r
                
                &7▶ 위치가 설정됨
                &6&l▶ &c위치가 설정되지 않음
                
                &e&l------------------
                """, type) :
                String.format(
                """
                &5&l%s&f&r
                
                &6&l▶ &9위치가 설정됨
                &7▶ 위치가 설정되지 않음
                
                %s
                &e&l------------------
                """, type, value.formattedLocation())));
    }
}
