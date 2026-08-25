package org.lazberry.xmaslegacy.collectors.game;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.collectors.field.Field;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.utils.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Session {
    @EqualsAndHashCode.Include
    private final CollectorsManager cm;
    private final Difficulty difficulty;
    private final BarCreator bar;
    private final Field field;
    private final Set<User> playingUsers = new HashSet<>(7);
    private AtomicTimer timer;

    public Session(Field field, CollectorsManager cm) {
        this.cm = cm;
        this.field = field;
        this.difficulty = field.getDifficulty();
        this.bar = BarCreator.create(ColorUtils.chat("&6&l남은시간"), difficulty.getColor());
        this.bar.setProgress(1.0f);
    }

    private Stream<Player> playerStream() {
        return playingUsers.stream()
                .map(User::getUniqueId)
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isValid)
                .filter(Player::isOnline);
    }

    public boolean addUser(User user) {
        OptionalUtils.ifNotNullOrElse(Bukkit.getPlayer(user.getUniqueId()),
                p -> {
                        cm.addBackup(p, p.getInventory().getContents());
                        p.getInventory().clear();
                        bar.addPlayer(p);
                }, () -> {});
        return playingUsers.add(user);
    }

    public boolean removeUser(User user) {
        OptionalUtils.ifNotNullOrElse(Bukkit.getPlayer(user.getUniqueId()),
                p -> {
                        cm.applyBackup(p);
                        bar.removePlayer(p);
                }, () -> {});
        return playingUsers.remove(user);
    }

    public boolean isSessionUser(User user) {
        return playingUsers.contains(user);
    }

    public boolean start() {
        if (timer == null || timer.isRunning() || field.isRunning()) return false;
        if (playingUsers.isEmpty()) return false;
        final int max = Math.toIntExact(difficulty.getDuration() / 20);

        field.setRunning(true);
        timer = new AtomicTimer(max, t -> {
            playerStream()
                    .forEach(p -> {
                        cm.applyWeightSlowness(p);
                        p.sendActionBar(ColorUtils.chat("&6무게&f " + cm.getWeight(p)));
                    });
            bar.setProgress(t.getRemainingSeconds(), max);
        }, () -> {
            playerStream().forEach(p -> {
                InfoUtils.error(p, "탐험에 실패했습니다! &c(타임오버)");
                p.getInventory().clear();
                p.setHealth(0);
                p.playSound(p, difficulty.getOverSound(), 1.0f, 1.0f);
                bar.removeAll();
            });
            playingUsers.clear();
            field.setRunning(false);
        });
        timer.start();
        return true;
    }

    public boolean stop() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            timer = null;

            bar.removeAll();
            return true;
        }
        field.setRunning(false);
        return false;
    }
}
