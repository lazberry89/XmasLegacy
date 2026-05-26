package xmasLegacy;

import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;

import java.util.UUID;

public class EffectListener implements Listener {
    private final SkillEffectManager sem;
    private final XmasLegacy plugin;

    public EffectListener() {
        this.plugin = XmasLegacy.getInstance();
        this.sem = SkillEffectManager.getInstance();
    }

    @EventHandler
    public void StunListener(EntityMoveEvent e) {
        LivingEntity le = e.getEntity();
        UUID uuid = le.getUniqueId();
        if (sem.stunMap().contains(uuid)) {
            e.setCancelled(true);
            le.sendActionBar(ColorUtils.chat(Alert.YELLOW + " 스턴상태"));
            //TODO Roped effect
        }
    }

    @EventHandler
    public void hideHiddenEntities(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        sem.getHiddenEntity().forEach(h -> p.hideEntity(plugin, h));
    }

    @EventHandler
    public void removeHidePlayer(PlayerQuitEvent e) {
        sem.showEntity(e.getPlayer());
    }
}
