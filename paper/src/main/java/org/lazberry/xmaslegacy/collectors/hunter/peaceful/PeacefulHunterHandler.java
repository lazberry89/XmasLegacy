package org.lazberry.xmaslegacy.collectors.hunter.peaceful;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.collectors.game.CollectorsManager;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.collectors.game.Session;
import org.lazberry.xmaslegacy.collectors.hunter.HunterHandler;
import org.lazberry.xmaslegacy.collectors.hunter.HunterRepository;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Registry.Include(type = ServerType.MAIN)
public class PeacefulHunterHandler extends HunterHandler {
	private static final long PEACEFUL_DESPAWN_TIME_MS = 60_000L;
	private final XmasLegacy plugin;
	private final Set<UUID> hunters = ConcurrentHashMap.newKeySet();
	private final Queue<DespawnEntry> despawnQueue = new ConcurrentLinkedQueue<>();
	private volatile BukkitTask task;

	@Inject
	public PeacefulHunterHandler(XmasLegacy plugin, HunterRepository repository, CollectorsManager cm) {
		super(cm, repository, Difficulty.PEACEFUL);
		this.plugin = plugin;
    }

	@Override
	public void init() {
		startDespawnTimer();
	}

	private void startDespawnTimer() {
		if (task == null) {
			task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

				long now = System.currentTimeMillis();

				while (!despawnQueue.isEmpty()) {
					DespawnEntry entry = despawnQueue.peek();
					if (entry == null || entry.expireTime() > now) break;
					UUID uuid = entry.uuid();

					despawnQueue.poll();
					hunters.remove(uuid);

					Entity entity = Bukkit.getEntity(uuid);
					Session session = cm().getOrCreateSession(Difficulty.PEACEFUL);
					if (session != null) {
						session.removeHunter(uuid);
					}
					if (entity != null && entity.isValid()) {
						entity.remove();
					}
				}
			}, 20L, 20L);
		}
	}

	private void stopTask() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	protected void removeHunters() {
		hunters.forEach(action());
		despawnQueue.clear();
		hunters.clear();
	}

	@Override
	public void close() {
		stopTask();
		removeHunters();
	}

	@Override
	public boolean spawnHunter(Location location) {
		return whenSpawnAvailable((s, h) -> {
			var uuid = h.spawn(location).getUniqueId();
			hunters.add(uuid);
			s.addHunter(uuid);
			despawnQueue.add(new DespawnEntry(uuid, System.currentTimeMillis() + PEACEFUL_DESPAWN_TIME_MS));
		}, () -> {});
	}

	private record DespawnEntry(UUID uuid, long expireTime) {}
}
