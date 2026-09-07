package org.lazberry.xmaslegacy.playerUtils.knockout;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.UUID;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class KnockoutListener implements Listener {
    private final KnockoutManager km;
    private final PartyManager pm;

    @Inject
    public KnockoutListener(KnockoutManager km, PartyManager pm) {
        this.km = km;
        this.pm = pm;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void knockdownWhenDeadIfHasParty(PlayerDeathEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!pm.isInParty(uuid) || KnockoutPlayer.isKnockedOut(player)) return;

        e.setCancelled(true);
        km.knockdownPlayer(player);
    }

    @EventHandler
    public void revivePartyPlayer(PlayerInteractEntityEvent e) {
        Player helper = e.getPlayer();
        UUID uuid = helper.getUniqueId();

        if (!(e.getRightClicked() instanceof Player downed)) return;
        if (!KnockoutPlayer.isKnockedOut(downed)) return;

        e.setCancelled(true);

        if (pm.isParty(uuid, downed.getUniqueId())) {
            km.clickProcess(helper, downed);
        }
    }
}
