package xmaslegacy.SavingLocation;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmaslegacy.SkillEffectManager;
import xmaslegacy.Utils.TitleUtil;

@Slf4j
public final class MainSpawnManager extends SavedLocation {

    public MainSpawnManager() {
        super(DestinationType.MAIN);
    }

    //Event priority = lowest
    public void joinEffect(@NotNull Player player) {
        Location to = super.getSpawn();
        if (to == null) {
            log.error("{} spawn is not set! Should set spawn first.", getType());
            if (!player.isOp()) player.kick(ColorUtils.chat("메인 서버가 미설정 상태입니다!"), PlayerKickEvent.Cause.PLUGIN);
            return;
        }

        SkillEffectManager.INSTANCE.StunEntity(player.getUniqueId(), 60L);
        TitleUtil.create("정보 로드중..", "", 5, 50, 5);
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 2, false, false, false));

        player.teleport(to);
    }
}
