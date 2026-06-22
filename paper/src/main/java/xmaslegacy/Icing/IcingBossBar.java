package xmaslegacy.Icing;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
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
            BossBar newBar = Bukkit.createBossBar(
                    ColorUtils.chatStr("&b&l[ 한기 수치 ]"),
                    BarColor.BLUE,
                    BarStyle.SOLID
            );
            newBar.addPlayer(p);
            return newBar;
        });

        double progress = Math.clamp(amount / 100.0, 0.0, 1.0);
        bar.setProgress(progress);

        if (amount <= 20) {
            bar.setColor(BarColor.RED);
            bar.setTitle(ColorUtils.chatStr("&4&l[ 한기수치 : 저체온증 진행 중 ]"));
        } else {
            bar.setColor(BarColor.BLUE);
            bar.setTitle(ColorUtils.chatStr("&b&l[ 한기 수치 ]"));
        }
    }

    public void removeBar(@NotNull Player p) {
        BossBar bar = bars.remove(p.getUniqueId());
        if (bar != null) {
            bar.removePlayer(p);
        }
    }
}
