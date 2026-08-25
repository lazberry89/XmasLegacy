package org.lazberry.xmaslegacy.collectors.drop;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.collectors.game.CollectorsManager;
import org.lazberry.xmaslegacy.collectors.game.Session;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class DropsListener implements Listener {
    private final CollectorsManager cm;
    private final DropsManager dm;
    private final UserManager um;

    @Inject
    public DropsListener(CollectorsManager cm, DropsManager dm, UserManager um) {
        this.cm = cm;
        this.dm = dm;
        this.um = um;
    }

    @EventHandler
    public void onPotHit(PlayerInteractEvent e) {
        Block targetBlock = e.getClickedBlock();
        Player player = e.getPlayer();
        User user = um.getUser(player.getUniqueId());
        if (user == null) return;

        Session session = cm.getSession(user);

        if (session == null || targetBlock == null || targetBlock.getType().isAir()) return;
        if (session.getField().isDropContainer(targetBlock)) {
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) dm.hitProcess(session, player, targetBlock);
            else e.setCancelled(true);
        }
    }
}
