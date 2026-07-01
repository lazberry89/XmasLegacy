package org.lazberry.xmaslegacy.Icing;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum IcingBossBar {
    INSTANCE;

    private final @NotNull Map<UUID, BossBar> bars = new HashMap<>();

    IcingBossBar() {}

    public void updateBar(@NotNull Player p, int amount) {
        UUID uuid = p.getUniqueId();

        BossBar bar = bars.computeIfAbsent(uuid, k -> {
            BossBar newBar = BossBar.bossBar(
                    ColorUtils.chat("&b&l[ 한기 수치 ]"),
                    1.0f,
                    BossBar.Color.BLUE,
                    BossBar.Overlay.PROGRESS
            );
            p.showBossBar(newBar);
            return newBar;
        });

        double progress = Math.clamp(amount / 100.0, 0.0, 1.0);
        bar.progress((float) progress);

        if (amount <= 20) {
            bar.color(BossBar.Color.RED);
            bar.name(ColorUtils.chat("&4&l[ 한기수치 : 저체온증 진행 중 ]"));
        } else {
            bar.color(BossBar.Color.BLUE);
            bar.name(ColorUtils.chat("&b&l[ 한기 수치 ]"));
        }
    }

    public void removeBar(@NotNull Player p) {
        BossBar bar = bars.remove(p.getUniqueId());
        if (bar != null) bar.removeViewer(p);
    }
}
