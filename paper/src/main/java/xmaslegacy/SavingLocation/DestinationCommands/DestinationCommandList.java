package xmaslegacy.SavingLocation.DestinationCommands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmaslegacy.SavingLocation.DestinationType;
import xmaslegacy.SavingLocation.SpawnRepository;
import xmaslegacy.Utils.SubCommand;

import java.util.Arrays;

public class DestinationCommandList implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        Arrays.stream(SpawnRepository.INSTANCE.availableTypes())
                .forEach(t -> sendFormatted(t, player));
    }

    private void sendFormatted(@NotNull DestinationType type, @NotNull Player p) {
        var value = SpawnRepository.INSTANCE.get(type);
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
