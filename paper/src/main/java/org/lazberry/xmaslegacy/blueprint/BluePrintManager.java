package org.lazberry.xmaslegacy.blueprint;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.blueprint.config.BlueprintConfig;
import org.lazberry.xmaslegacy.blueprint.config.BlueprintTaskRecordConfig;
import org.lazberry.xmaslegacy.region.RegionManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.Axiom;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Registry.Include(type = {ServerType.MAIN, ServerType.WILD})
public class BluePrintManager implements Initiator {
    private final Map<String, BluePrint> bluePrints = new ConcurrentHashMap<>();
	private final Map<UUID, ActiveTask> buildTasks = new ConcurrentHashMap<>();
    private final Map<UUID, TaskRecord> stoppedTaskRecords = new ConcurrentHashMap<>();
    private final RegionManager rm;
	private final BlueprintConfig config;
	private final BlueprintTaskRecordConfig taskConfig;
    private final XmasLegacy plugin;

	@Getter @Setter
    private long delayPerBlock = 5L;

    @Inject
    public BluePrintManager(RegionManager rm, BlueprintConfig config,
                            BlueprintTaskRecordConfig taskConfig, XmasLegacy plugin) {
        this.rm = rm;
	    this.config = config;
	    this.taskConfig = taskConfig;
	    this.plugin = plugin;
    }

    @Override
    public void init() {
		bluePrints.clear();
		bluePrints.putAll(config.loadSync());

		delayPerBlock = taskConfig.getTaskDelay();
		stoppedTaskRecords.putAll(taskConfig.loadSync());

		Bukkit.getScheduler().runTaskLater(plugin, this::resumeBuilding, 20 * 30);
    }

    public boolean exists(String id) {
        return bluePrints.containsKey(id);
    }

	public Collection<BluePrint> getBluePrints() {
		return bluePrints.values();
	}

    public Optional<BluePrint> getBluePrint(String structureName) {
        return Optional.ofNullable(bluePrints.get(structureName));
    }

    public BluePrint createBluePrint(String id, Location origin, Location loc1, Location loc2) {
        BluePrint bluePrint = new BluePrint(id);
        Axiom.loopArea(loc1, loc2, l -> {
            var block = l.getBlock();
            if (block.getType().isAir()) return;

            bluePrint.addBlock(origin, l, block);
        });
        return bluePrint;
    }

    public boolean register(BluePrint bluePrint) {
        var name = bluePrint.getStructureName();
        if (exists(name)) return false;
        bluePrints.put(name, bluePrint);
        return true;
    }

    public boolean canBuild(BluePrint bluePrint, Player builder, Location origin) {
        var regions = rm.getRegion(builder);
        if (regions.isEmpty()) return false;

        return bluePrint.matchAllVirtualLocations(origin, l -> {
            var region = rm.getRegionAt(l);
            return region != null && regions.contains(region);
        });
    }

	public void startBuilding(String id, Player builder, Runnable done) {
		var startLoc = builder.getLocation();
		var uuid = builder.getUniqueId();

		BluePrint value = bluePrints.get(id);
		if (value == null) {
			InfoUtils.error(builder, "존재하지 않는 건축물 이름입니다.");
			return;
		}
		if (buildTasks.containsKey(uuid)) {
			InfoUtils.error(builder, "이미 진행중인 건축물이 있습니다.");
			InfoUtils.warn(builder, "완료 후 다른 작업이 가능합니다.");
			return;
		}

		if (canBuild(value, builder, startLoc)) {
			long expectedTime = value.getMaxProcess() * delayPerBlock;
			InfoUtils.info(builder, "건축이 시작됩니다. &6(예상시간: {})",
					Axiom.formatTicksToMMSS(expectedTime));

			BluePrint session = value.copy();
			var current = System.currentTimeMillis();
			var task = new BukkitRunnable() {

				@Override
				public void run() {
					boolean isDone = !session.process(startLoc, BluePrintManager.this::playEffectWhenPlace);

					if (isDone) {
						this.cancel();
						buildTasks.remove(uuid);
						if (done != null) done.run();

						Player p = Bukkit.getPlayer(uuid);
						if (p != null && p.isOnline()) {
							long expectedMillis = expectedTime * 50L;
							long exactTime = System.currentTimeMillis() - current;
							long delayMillis = exactTime - expectedMillis;

							String extraInfo = delayMillis > 0 ? " &c(+ " + Axiom.formatTicksToMMSS(Axiom.millisToTicks(delayMillis)) + ")" : "";
							InfoUtils.info(p, "작업이 완료되었습니다! &6(소요시간: {}{})",
									Axiom.formatTicksToMMSS(Axiom.millisToTicks(exactTime)), extraInfo);
						}
					}
				}

			}.runTaskTimer(plugin, 0L, delayPerBlock);
			ActiveTask activeTask = new ActiveTask(task, session, startLoc, current, 0L);
			buildTasks.put(uuid, activeTask);
		} else InfoUtils.error(builder, "본인 소유의 구역 내부에서만 건축이 가능합니다.");
	}

    public boolean hasBuildTask(UUID uuid) {
        return buildTasks.containsKey(uuid);
    }

    public boolean hasStoppedBuildTask(UUID uuid) {
        return stoppedTaskRecords.containsKey(uuid);
    }

	public void resumeBuilding() {
		if (stoppedTaskRecords.isEmpty()) return;

		var records = new java.util.HashMap<>(stoppedTaskRecords);
		stoppedTaskRecords.clear();

		records.forEach((uuid, record) -> {
			BluePrint value = bluePrints.get(record.structureName());
			if (value == null) return;

			var startLoc = record.startLocation();
			if (startLoc == null || startLoc.getWorld() == null) return;

			BluePrint session = value.copy();
			int maxProcess = session.getMaxProcess();
			int startProcess = record.process();

			var current = System.currentTimeMillis();
			var task = new BukkitRunnable() {
				boolean firstTick = true;

				@Override
				public void run() {
					boolean isDone;
					Consumer<Location> particleSound = BluePrintManager.this::playEffectWhenPlace;

					if (firstTick) {
						isDone = !session.processFrom(startLoc, startProcess, particleSound);
						firstTick = false;
					} else {
						isDone = !session.process(startLoc, particleSound);
					}

					if (isDone) {
						this.cancel();
						buildTasks.remove(uuid);

						// TODO: 오프라인 건축 완공 알림 처리 (현재는 온라인 상태일 때만 즉시 전송)
						Player p = org.bukkit.Bukkit.getPlayer(uuid);
						if (p != null && p.isOnline()) {
							long expectedTotalMillis = maxProcess * delayPerBlock * 50L;
							long exactTotalTime = record.elapsedMillis() + (System.currentTimeMillis() - current);
							long delayMillis = exactTotalTime - expectedTotalMillis;

							String extraInfo = delayMillis > 0 ? " &c(+ " + Axiom.formatTicksToMMSS(Axiom.millisToTicks(delayMillis)) + ")" : "";
							InfoUtils.info(p, "작업이 완료되었습니다! &6(총 소요시간: {}{})",
									Axiom.formatTicksToMMSS(Axiom.millisToTicks(exactTotalTime)), extraInfo);
						}
					}
				}
			}.runTaskTimer(plugin, 0L, delayPerBlock);

			ActiveTask activeTask = new ActiveTask(task, session, startLoc, current, record.elapsedMillis());
			buildTasks.put(uuid, activeTask);
		});
	}

    private void playEffectWhenPlace(Location loc) {
        loc.getWorld().spawnParticle(Particle.ASH, loc, 5, 0.3, 0.3, 0.3, 0.01);
        var block = loc.getBlock();
        if (!block.getType().isAir()) {
            BlockData data = block.getBlockData();
            Sound placeSound = data.getSoundGroup().getPlaceSound();
            loc.getWorld().playSound(loc, placeSound, 1.0f, 1.0f);
        }
    }

    @Override
    public void close() {
	    buildTasks.forEach((uuid, activeTask) -> {
		    activeTask.task().cancel();
		    stoppedTaskRecords.put(uuid, activeTask.toTaskRecord(uuid));
	    });
	    buildTasks.clear();

	    config.saveSync(bluePrints);
	    bluePrints.clear();

	    taskConfig.saveSync(stoppedTaskRecords);
	    stoppedTaskRecords.clear();
    }

	private record ActiveTask(
			BukkitTask task,
			BluePrint session,
			Location startLoc,
			long startTimeMillis,
			long previousElapsedMillis
	) {
		public TaskRecord toTaskRecord(UUID uuid) {
			long currentElapsed = System.currentTimeMillis() - startTimeMillis;
			return new TaskRecord(uuid, session.getStructureName(), session.getProcess(), startLoc, previousElapsedMillis + currentElapsed);
		}
	}
    public record TaskRecord(UUID builder, String structureName, int process, Location startLocation, long elapsedMillis) {}
}
