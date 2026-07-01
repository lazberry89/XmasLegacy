package org.lazberry.xmaslegacy.SavingLocation.DestinationCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.SavingLocation.DestinationType;
import org.lazberry.xmaslegacy.SavingLocation.PortVillageManager;
import org.lazberry.xmaslegacy.SavingLocation.SpawnRepository;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.ServerTransfer;
import org.lazberry.xmaslegacy.Utils.SubCommand;

import java.time.Duration;

public record DestinationCommandMove(@NotNull DestinationType type) implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (type.equals(DestinationType.FROZEN_PORT)) {
            PortVillageManager value = SpawnRepository.INSTANCE.get(type);
            value.move(player);
            return;
        }
        var value = SpawnRepository.INSTANCE.get(type);
        Location from = player.getLocation();
        Location to = value.getSpawn();
        if (to == null) {
            InfoUtils.error(player, "위치가 설정되지 않은 목적지입니다. 설정 후 사용해주세요.");
            return;
        }
        ServerTransfer.dramaticTeleport(player, to);
        ClickCallback.Options option = ClickCallback.Options.builder()
                .uses(1)
                .lifetime(Duration.ofMinutes(5))
                .build();

        Component back = ColorUtils.chat("&c&l[돌아가기]")
                .hoverEvent(HoverEvent.showText(ColorUtils.chat("클릭하여 기존 위치로 돌아갈 수 있습니다.")))
                        .clickEvent(ClickEvent.callback(a -> {
                            ServerTransfer.dramaticTeleport(player, from);
                            InfoUtils.info(player, "기존 위치로 돌아갔습니다.");
                        }, option));
        InfoUtils.info(player, ColorUtils.chat("이동하였습니다.").appendSpace().append(back));
    }
}
