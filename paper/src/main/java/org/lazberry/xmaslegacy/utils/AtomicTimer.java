package org.lazberry.xmaslegacy.utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmaslegacy.XmasLegacy;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class AtomicTimer {
    private final XmasLegacy plugin;
    private final AtomicInteger remainingSeconds;
    private final Consumer<AtomicTimer> onTick;
    private final Runnable onExpire;
    private BukkitTask task;
    private @Getter boolean running = false;
    private @Setter boolean paused = false;

    public AtomicTimer(int initialSeconds, Consumer<AtomicTimer> onTick, Runnable onExpire) {
        this.plugin = XmasLegacy.getInstance();
        this.remainingSeconds = new AtomicInteger(initialSeconds);
        this.onTick = onTick;
        this.onExpire = onExpire;
    }

    public synchronized void start() {
        if (running) return;
        running = true;

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (paused) return;

                int current = remainingSeconds.get();

                if (current <= 0) {
                    stop();
                    if (onExpire != null) onExpire.run();
                    return;
                }

                remainingSeconds.decrementAndGet();
                if (onTick != null) {
                    onTick.accept(AtomicTimer.this);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20틱 = 1초
    }

    public int overtime(int seconds) {
        if (seconds <= 0) return remainingSeconds.get();
        return remainingSeconds.addAndGet(seconds);
    }

    public int decreaseTime(int seconds) {
        if (seconds <= 0) return remainingSeconds.get();
        return remainingSeconds.updateAndGet(current -> Math.max(0, current - seconds));
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public int getRemainingSeconds() {
        return remainingSeconds.get();
    }
}
