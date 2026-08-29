package org.lazberry.xmaslegacy.collectors.hunter.exciting;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.collectors.game.CollectorsManager;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.collectors.hunter.HunterHandler;
import org.lazberry.xmaslegacy.collectors.hunter.HunterRepository;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.CollectorFactory;

import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Registry.Include(type = ServerType.MAIN)
public class PhaseHunterHandler extends HunterHandler {
    private static final long AGGRO_DURATION_MS = 10_000L;
    private final Set<UUID> hunters = ConcurrentHashMap.newKeySet(5);
    private final Queue<AggroEntry> aggroQueue = new ConcurrentLinkedQueue<>();
    private final XmasLegacy plugin;
    private final PhaseHunter hunterInstance;
    private volatile BukkitTask task;

    @Inject
    public PhaseHunterHandler(CollectorsManager cm, HunterRepository repository, XmasLegacy plugin) {
        super(cm, repository, Difficulty.EXCITING);
        this.plugin = plugin;
        this.hunterInstance = repository().getHunter(Difficulty.EXCITING, PhaseHunter.class);
    }

    @Override
    public void init() {
        startPhaseTask();
    }

    public void startPhaseTask() {
        if (task == null) {
            synchronized (this) {
                if (task == null) {
                    task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        processPhaseEffect();
                        processAggroTimer();
                    }, 0L, 2L);
                }
            }
        }
    }

    public void triggerAggro(WitherSkeleton hunter) {
        if (!PhaseHunter.isEntity(hunter) || !PhaseHunter.isPhase(hunter)) return;

        hunterInstance.dephase(hunter);
        aggroQueue.add(new AggroEntry(hunter.getUniqueId(), System.currentTimeMillis() + AGGRO_DURATION_MS));
    }

    private void processPhaseEffect() {
        hunters.stream()
                .map(Bukkit::getEntity)
                .filter(Objects::nonNull)
                .filter(Entity::isValid)
                .filter(WitherSkeleton.class::isInstance)
                .map(WitherSkeleton.class::cast)
                .filter(PhaseHunter::isEntity)
                .collect(CollectorFactory.toPartition(PhaseHunter::isPhase))
                .matches(h -> h.getWorld().spawnParticle(Particle.SCULK_SOUL, h.getLocation().add(0, 1, 0), 2, 0.1, 0.2, 0.1, 0.01));
    }

    private void processAggroTimer() {
        long now = System.currentTimeMillis();

        while (!aggroQueue.isEmpty()) {
            AggroEntry entry = aggroQueue.peek();
            if (entry == null || entry.expireTime() > now) {
                break;
            }

            aggroQueue.poll();
            Entity entity = Bukkit.getEntity(entry.uuid());
            if (entity instanceof WitherSkeleton w && w.isValid()) {
                hunterInstance.phase(w);
            }
        }
    }

    @Override
    public boolean spawnHunter(Location location) {
        return whenSpawnAvailable((s, h) -> {
            WitherSkeleton spawned = (WitherSkeleton) h.spawn(location);
            UUID uuid = spawned.getUniqueId();
            hunterInstance.phase(spawned);
            hunters.add(uuid);
            s.addHunter(uuid);
        }, () -> {});
    }

    @Override
    protected void removeHunters() {
        hunters.forEach(action());
        aggroQueue.clear();
        hunters.clear();
    }

    @Override
    public void close() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        removeHunters();
    }

    private record AggroEntry(UUID uuid, long expireTime) {}
}