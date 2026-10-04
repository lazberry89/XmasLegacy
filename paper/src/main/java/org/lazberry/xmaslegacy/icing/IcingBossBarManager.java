package org.lazberry.xmaslegacy.icing;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class IcingBossBarManager implements Initiator {
    private final Map<UUID, BossBar> bars = new HashMap<>();

    public IcingBossBarManager() {}

	@Override
	public void init() {
		log.info("Icing manager started.");
	}

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

	public void clearAll() {
		Bukkit.getOnlinePlayers().forEach(p -> {
			UUID uuid = p.getUniqueId();
			BossBar bar = bars.get(uuid);
			if (bar != null) bar.removeViewer(p);
		});
		bars.clear();
	}

	@Override
	public void close() {
		clearAll();
	}
}
