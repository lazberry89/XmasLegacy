package org.lazberry.xmaslegacy.collectors.hunter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.collectors.game.CollectorsManager;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.collectors.game.Session;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Registry.Include(type = ServerType.MAIN)
public class HunterManager implements Initiator {
	private static final long PEACEFUL_DESPAWN_TIME_MS = 60_000L;
	private final XmasLegacy plugin;
	private final HunterRepository repository;
	private final CollectorsManager cm;
	private final Set<UUID> peacefulHunters = ConcurrentHashMap.newKeySet();
	private final Set<UUID> excitingHunters = new HashSet<>(5);
	private final Set<UUID> horrorHunters = new HashSet<>(5);
	private final Queue<DespawnEntry> despawnQueue = new ConcurrentLinkedQueue<>();
	private volatile BukkitTask task;
	private final Consumer<UUID> action = uuid -> {
		Entity entity = Bukkit.getEntity(uuid);
		if (entity != null && entity.isValid()) entity.remove();
	};

	@Inject
	public HunterManager(XmasLegacy plugin, HunterRepository repository, CollectorsManager cm) {
		this.plugin = plugin;
		this.repository = repository;
        this.cm = cm;
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
					peacefulHunters.remove(uuid);

					Entity entity = Bukkit.getEntity(uuid);
					Session session = cm.getOrCreateSession(Difficulty.PEACEFUL);
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

	private void removeHunters() {
		peacefulHunters.forEach(action);
		excitingHunters.forEach(action);
		horrorHunters.forEach(action);
		despawnQueue.clear();
		peacefulHunters.clear();
		excitingHunters.clear();
		horrorHunters.clear();
	}

	@Override
	public void close() {
		stopTask();
		removeHunters();
	}

	private record DespawnEntry(UUID uuid, long expireTime) {}

	private boolean whenSpawnAvailable(@NotNull Difficulty difficulty,
									   BiConsumer<@NotNull Session, @NotNull Hunter> available,
									   Runnable fail) {
		Session session = cm.getOrCreateSession(difficulty);
		if (session == null || !session.getField().isRunning()) {
			fail.run();
			return false;
		}
		Hunter hunter = switch (difficulty) {
			case PEACEFUL -> repository.getHunter(difficulty, HunterBug.class);
			case EXCITING -> repository.getHunter(difficulty, PhaseHunter.class);
			case HORROR -> repository.getHunter(difficulty, Silence.class);
		};
		available.accept(session, hunter);
		return true;
	}

	public boolean spawnPeacefulHunter(Location location) {
		return whenSpawnAvailable(Difficulty.PEACEFUL, (s, h) -> {
			var uuid = h.spawn(location).getUniqueId();
			peacefulHunters.add(uuid);
			s.addHunter(uuid);
			despawnQueue.add(new DespawnEntry(uuid, System.currentTimeMillis() + PEACEFUL_DESPAWN_TIME_MS));
		}, () -> {});
	}

	public boolean spawnExcitingHunter(Location location) {
		return whenSpawnAvailable(Difficulty.EXCITING, (s, h) -> {
			excitingHunters.add(h.spawn(location).getUniqueId());
		}, () -> {});
	}

	public boolean spawnHorrorHunter(Location location) {
		return whenSpawnAvailable(Difficulty.HORROR, (s, h) -> {

		}, () -> {});
	}

}
