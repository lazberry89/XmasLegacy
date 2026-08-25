package org.lazberry.xmaslegacy.collectors.backup;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.collectors.game.CollectorsManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class BackupListener implements Listener {
    private final CollectorsManager cm;

    @Inject
    public BackupListener(CollectorsManager cm) {
        this.cm = cm;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void loadBackupWhenJoin(PlayerJoinEvent e) {
        var player = e.getPlayer();
        var uuid = player.getUniqueId();
        if (cm.hasBackup(uuid)) {
            cm.applyBackup(player);
            InfoUtils.info(player, "아이템이 백업되었습니다.");
            InfoUtils.warn(player, "만약 백업이 완전하지 않다면 관리자에게 문의해주세요.");
        }
    }
}
