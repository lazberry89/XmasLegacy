package org.lazberry.xmaslegacy.wild.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.wild.PortalCountModule;

@Listeners
@Components.Include(type = ServerType.WILD)
public class PortalUseListener implements Listener {
    private final PortalCountModule countModule;

    @Inject
    public PortalUseListener(PortalCountModule countModule) {
        this.countModule = countModule;
    }

    @EventHandler
    public void onWorldChange(PlayerPortalEvent e) {
        var p = e.getPlayer();

        var item = p.getInventory().getItemInMainHand();
        if (PortalCountModule.isModule(item)) {
            e.setCancelled(!countModule.useModule(p, item));
        } else {
            e.setCancelled(true);
            InfoUtils.error(p, "포탈 모듈을 손에 들어주세요.");
        }
    }
}
