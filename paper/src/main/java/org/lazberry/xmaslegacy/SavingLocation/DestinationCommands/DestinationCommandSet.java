package org.lazberry.xmaslegacy.SavingLocation.DestinationCommands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.SavingLocation.DestinationType;
import org.lazberry.xmaslegacy.SavingLocation.SpawnRepository;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;

public record DestinationCommandSet(@NotNull DestinationType type) implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull...args) {
        var value = SpawnRepository.INSTANCE.get(type);
        Location loc = player.getLocation();

        value.setSpawn(loc);
        InfoUtils.info(player, "스폰 위치가 현재 위치로 설정되었습니다.");
        InfoUtils.info(player, String.format("world : %s, x: %.1f, y: %.1f, z: %.1f, yaw: %.1f, pitch: %.1f",
                loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch()));
    }
}
