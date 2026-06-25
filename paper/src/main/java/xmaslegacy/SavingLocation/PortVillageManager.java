package xmaslegacy.SavingLocation;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.ServerTransfer;

import java.util.Objects;

@Slf4j
public final class PortVillageManager extends SavedLocation {

    public PortVillageManager() {
        super(DestinationType.FROZEN_PORT);
    }

    public void move(@NotNull Player player) {
        Location to = super.getSpawn();
        if (to == null) {
            log.error("Port location is not set! Should set location first.");
            Bukkit.getOnlinePlayers().stream()
                    .filter(Objects::nonNull)
                    .filter(Entity::isValid)
                    .filter(Player::isOp)
                    .forEach(o -> {
                        InfoUtils.error(o, "얼어붙은 항구의 스폰 위치가 설정되지 않았습니다.");
                        InfoUtils.error(o, "명령어를 사용하여 즉시 등록하여 주세요.");
                    });
            return;
        }
        ServerTransfer.dramaticTeleport(player, to, 60L);
    }
}
