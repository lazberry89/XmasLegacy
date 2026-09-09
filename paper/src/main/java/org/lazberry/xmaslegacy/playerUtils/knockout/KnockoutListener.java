package org.lazberry.xmaslegacy.playerUtils.knockout;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.UUID;

@Listeners
@Registry.Exclude(type = ServerType.LOBBY)
public class KnockoutListener implements Listener {
	private final NamespacedKey key;
    private final KnockoutManager km;
    private final PartyManager pm;

    @Inject
    public KnockoutListener(KnockoutManager km, PartyManager pm) {
		this.key = KnockoutPlayer.left;
        this.km = km;
        this.pm = pm;
    }

	@EventHandler(priority = EventPriority.LOW)
	public void knockdownWhenDeadIfHasParty(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player player)) return;
		UUID uuid = player.getUniqueId();

		if (!pm.isInParty(uuid) || KnockoutPlayer.isKnockedOut(player)) return;

		if (player.getHealth() - e.getFinalDamage() <= 0) {
			e.setCancelled(true);
			km.knockdownPlayer(player);
		}
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

	@EventHandler
	public void ifDeadWhenKnockedDown(PlayerDeathEvent e) {
		if (e.isCancelled()) return;
		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();

		if (KnockoutPlayer.isKnockedOut(player)) {
			km.getKnockoutPlayer(uuid).ifPresent(km::removeKnockoutPlayer);
		}
	}

	@EventHandler
	public void playerQuitServerWhileKnockout(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (KnockoutPlayer.isKnockedOut(p)) {
			km.removeKnockoutPlayer(p);
			p.setHealth(0.0);
			KeyUtils.set(p, key, true);
		}
	}

	@EventHandler
	public void informPlayerIfLeftWhileKnockout(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (KeyUtils.hasKey(p, KnockoutPlayer.left)) {
			KeyUtils.remove(p, key);
			InfoUtils.warn(p, "기절 도중 퇴장하여 사망처리되었습니다.");
		}
	}
}
