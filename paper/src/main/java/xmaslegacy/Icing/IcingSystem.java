package xmaslegacy.Icing;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public enum IcingSystem {
    INSTANCE;

    private final @NotNull Map<UUID, Integer> icingMap = new HashMap<>();
    private final @NotNull Random random = new Random();
    private @Nullable BukkitTask task;

    IcingSystem() {}

    public int getState(@NotNull UUID uuid) {
        return this.icingMap.getOrDefault(uuid, 100);
    }

    public void setState(@NotNull UUID uuid, int amount) {
        this.icingMap.put(uuid, Math.max(0, amount));
    }

    public void startTask(@NotNull XmasLegacy plugin) {
        if (this.task != null) return;
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (p.isInvulnerable()) return;
                var uuid = p.getUniqueId();
                var user = UserManager.INSTANCE.getUser(uuid);
                if (user == null || user.isImmuneToIcing()) {
                    IcingBossBar.INSTANCE.removeBar(p);
                    return;
                }

                int amount = this.getState(uuid);
                int nextAmount = amount - 1;
                this.setState(uuid, amount - 1);

                IcingBossBar.INSTANCE.updateBar(p, nextAmount);

                if (nextAmount <= 20) {
                    p.setFreezeTicks(150);
                    p.sendActionBar(ColorUtils.chat(String.format("&4&l[ 빙결수치 경고 : %d%% ]", nextAmount)));

                    if (nextAmount == 0) {
                        p.damage(9.0);
                        p.playSound(p, Sound.BLOCK_BELL_USE, 1.0f, 0.5f);
                    }
                }
            });
        }, 0L, 20 * 3);
    }

    public void stopTask() {
        if (this.task == null) return;
        this.task.cancel();
        this.task = null;
    }
}
