package org.lazberry.xmaslegacy.utils;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BarCreator {
    private final BossBar bossBar;
    private final Set<UUID> viewers = new HashSet<>();

    public BarCreator(Component title, float progress, BossBar.Color color, BossBar.Overlay overlay) {
        float safeProgress = Math.clamp(progress, 0.0f, 1.0f);
        this.bossBar = BossBar.bossBar(title, safeProgress, color, overlay);
    }

    public static BarCreator create(Component title, BossBar.Color color) {
        return new BarCreator(title, 1.0f, color, BossBar.Overlay.PROGRESS);
    }

    public void addPlayer(@Nullable Player player) {
        if (player == null) return;
        if (viewers.add(player.getUniqueId())) {
            player.showBossBar(bossBar);
        }
    }

    public void removePlayer(@Nullable Player player) {
        if (player == null) return;
        if (viewers.remove(player.getUniqueId())) {
            player.hideBossBar(bossBar);
        }
    }

    public void removeAll() {
        for (UUID uuid : viewers) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.hideBossBar(bossBar);
            }
        }
        viewers.clear();
    }

    public BarCreator setProgress(float progress) {
        this.bossBar.progress(Math.clamp(progress, 0.0f, 1.0f));
        return this;
    }

    public BarCreator setProgress(int current, int max) {
        if (max <= 0) {
            return setProgress(0f);
        }
        return setProgress((float) current / max);
    }

    public BarCreator setTitle(Component title) {
        this.bossBar.name(title);
        return this;
    }

    public BarCreator setColor(BossBar.Color color) {
        this.bossBar.color(color);
        return this;
    }

    public BarCreator setOverlay(BossBar.Overlay overlay) {
        this.bossBar.overlay(overlay);
        return this;
    }

    public BossBar getOriginal() {
        return this.bossBar;
    }
}
