package org.lazberry.xmaslegacy.collectors.hunter;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.collectors.game.CollectorsManager;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.collectors.game.Session;
import org.lazberry.xmaslegacy.collectors.hunter.exciting.PhaseHunter;
import org.lazberry.xmaslegacy.collectors.hunter.horror.Silence;
import org.lazberry.xmaslegacy.collectors.hunter.peaceful.HunterBug;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.utils.KeyUtils;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class HunterHandler implements Initiator {
    private final CollectorsManager cm;
    private final HunterRepository repository;
    private final Difficulty difficulty;
    private final NamespacedKey key;
    private final Consumer<UUID> action = uuid -> {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity != null && entity.isValid()) entity.remove();
    };

    protected HunterHandler(CollectorsManager cm, HunterRepository repository, Difficulty difficulty) {
        this.cm = cm;
        this.repository = repository;
        this.difficulty = difficulty;
        this.key = KeyUtils.get(difficulty.name() + "_hunter");
    }

    public CollectorsManager cm() {
        return cm;
    }
    public HunterRepository repository() {
        return repository;
    }
    public Difficulty difficulty() {
        return difficulty;
    }
    public NamespacedKey key() {
        return key;
    }
    public Consumer<UUID> action() {
        return action;
    }

    protected boolean whenSpawnAvailable(BiConsumer<@NotNull Session, @NotNull Hunter> available, Runnable fail) {
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

    public abstract boolean spawnHunter(Location location);
    protected abstract void removeHunters();
}
