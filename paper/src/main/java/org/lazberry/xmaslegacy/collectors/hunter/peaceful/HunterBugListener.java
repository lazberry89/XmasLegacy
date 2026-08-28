package org.lazberry.xmaslegacy.collectors.hunter.peaceful;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.collectors.drop.PlayerBreakContainerEvent;
import org.lazberry.xmaslegacy.collectors.drop.PlayerHitContainerEvent;
import org.lazberry.xmaslegacy.collectors.game.CollectorsManager;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.collectors.game.Session;
import org.lazberry.xmaslegacy.collectors.hunter.HunterRepository;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.OptionalUtils;

import java.util.UUID;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class HunterBugListener implements Listener {
	private final HunterRepository repository;
	private final PeacefulHunterHandler handler;
	private final CollectorsManager cm;
	private final UserManager um;

	@Inject
    public HunterBugListener(HunterRepository repository, PeacefulHunterHandler handler, CollectorsManager cm, UserManager um) {
        this.repository = repository;
        this.handler = handler;
        this.cm = cm;
        this.um = um;
    }

    @EventHandler
	public void spawnHunterBugWhenPlayerBreakContainer(PlayerBreakContainerEvent e) {
		Session session = e.getSession();
		Location loc = e.getLocation();
		Difficulty difficulty = session.getDifficulty();
		Player p = e.getPlayer();

		if (difficulty == Difficulty.PEACEFUL) {
			HunterBug hunter = repository.getHunter(difficulty, HunterBug.class);
			int loop = hunter.getRandomSpawnCount();
			if (loop == 0) return;
			for (int i = 0; i < loop; i++) {
				handler.spawnHunter(loc);
			}

			e.getAffectedEntities().forEach(lv -> lv.attack(p));
		}
	}

    @EventHandler
	public void onPlayerHit(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player victim)) return;
		var damager = e.getDamager();
		UUID uuid = victim.getUniqueId();

		OptionalUtils.ifNotNull(um.getUser(uuid), u -> {
			Session session = cm.getSession(u);
			if (session == null || session.getDifficulty() != Difficulty.PEACEFUL) return;
			if (HunterBug.isEntity(damager)) {
				HunterBug hunter = repository.getHunter(Difficulty.PEACEFUL, HunterBug.class);
				hunter.attack(victim);
			}
		});
	}
}
