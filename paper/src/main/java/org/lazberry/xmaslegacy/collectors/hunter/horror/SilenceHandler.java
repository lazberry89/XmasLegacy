package org.lazberry.xmaslegacy.collectors.hunter.horror;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.TitleUtil;
import org.lazberry.xmaslegacy.utils.Vignette;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Registry.Include(type = ServerType.MAIN)
public class SilenceHandler extends HunterHandler {
    private static final long RAGE_CYCLE_TICKS = 600L; // 30초 주기
    private static final long RAGE_DURATION_TICKS = 200L; // 분노 10초 유지
    private final Set<UUID> hunters = ConcurrentHashMap.newKeySet(5);
    private final Silence hunterInstance;
    private final UserManager um;
    private final XmasLegacy plugin;

    private volatile BukkitTask task;
    private long currentTick = 0L;
    private @Getter boolean furious = false;

    @Inject
    public SilenceHandler(CollectorsManager cm, HunterRepository repository, UserManager um, XmasLegacy plugin) {
        super(cm, repository, Difficulty.HORROR);
        this.um = um;
        this.plugin = plugin;
        this.hunterInstance = repository().getHunter(Difficulty.HORROR, Silence.class);
    }

    @Override
    public void init() {
        startRageTask();
    }

    public void startRageTask() {
        if (task == null) synchronized (this) {
            if (task == null)
                task = Bukkit.getScheduler().runTaskTimer(plugin,
                        this::processTimer, 0L, 20L);
        }
    }

    private void processTimer() {
        boolean isAnyoneChasing = hunters.stream()
                .map(Bukkit::getEntity)
                .filter(Objects::nonNull)
                .anyMatch(Silence::isChasing);

        if (isAnyoneChasing) return;

        currentTick += 20L;
        long remainTicks = RAGE_CYCLE_TICKS - currentTick;

        if (remainTicks == 60L) {
            sendAlertToSessionPlayer("&4움직이지 마세요.");
        } else if (remainTicks == 40L) {
            sendAlertToSessionPlayer("&42초 남음");
        } else if (remainTicks == 20L) {
            sendAlertToSessionPlayer("&41초남음");
        }

        if (currentTick == RAGE_CYCLE_TICKS) {
            setFuriousAll(true);
            furious = true;
            playerStream().forEach(p -> {
                p.playSound(p, Sound.ENTITY_WARDEN_ROAR, 1.5f, 0.8f);
                Vignette.sendVignetteEffect(p);
                p.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 200, 1, true, false, false));
            });
        }

        if (currentTick >= (RAGE_CYCLE_TICKS + RAGE_DURATION_TICKS)) {
            setFuriousAll(false);
            currentTick = 0L;
            furious = false;
            playerStream().forEach(p -> {
                Vignette.clearVignette(p);
                p.playSound(p, Sound.ENTITY_PLAYER_BREATH, 1.5f, 1.0f);
                p.removePotionEffect(PotionEffectType.DARKNESS);
            });
        }
    }

    private void sendAlertToSessionPlayer(String message) {
        playerStream()
                .forEach(p -> {
                    p.showTitle(TitleUtil.create("", message));
                    p.playSound(p, Sound.BLOCK_BELL_USE, 1.0f, 0.9f);
                });
    }

    private Stream<Player> playerStream() {
        Session session = cm().getSession(Difficulty.HORROR);
        if (session == null) return Stream.empty();

        return session.getPlayingUsers().stream()
                .filter(Objects::nonNull)
                .map(User::getUniqueId)
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Entity::isValid)
                .filter(Player::isOnline);
    }

    private void setFuriousAll(boolean furious) {
        hunters.stream()
                .map(Bukkit::getEntity)
                .filter(Objects::nonNull)
                .filter(Warden.class::isInstance)
                .forEach(w -> Silence.setFurious(w, furious));
    }

    @Override
    public boolean spawnHunter(Location location) {
        return whenSpawnAvailable((s, h) -> {
            Warden spawned = (Warden) h.spawn(location);
            UUID uuid = spawned.getUniqueId();
            hunters.add(uuid);
            s.addHunter(uuid);
        }, () -> {});
    }

    @Override
    protected void removeHunters() {
        hunters.forEach(action());
        hunters.clear();
        currentTick = 0L;
    }

    @Override
    public void close() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        removeHunters();
        super.close();
    }
}