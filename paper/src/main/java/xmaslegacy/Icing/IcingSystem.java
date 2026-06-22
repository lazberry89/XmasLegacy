package xmaslegacy.Icing;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.XmasLegacy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum IcingSystem {
    INSTANCE;

    private @Nullable BukkitTask task;

    IcingSystem() {}

    public void startTask(@NotNull XmasLegacy plugin) {
        if (this.task != null) return;
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () ->
            Bukkit.getOnlinePlayers().forEach(p -> {
                if (p.isInvulnerable()
		                || p.getGameMode() == GameMode.CREATIVE
		                || p.getGameMode() == GameMode.SPECTATOR) return;
                var uuid = p.getUniqueId();
                var user = UserManager.INSTANCE.getUser(uuid);
                if (user == null || user.isImmuneToIcing()) {
                    IcingBossBar.INSTANCE.removeBar(p);
                    return;
                }

                int amount = user.getIcingState();
				int nextAmount = Math.max(0, amount - 1);
                user.setIcingState(nextAmount);

                IcingBossBar.INSTANCE.updateBar(p, nextAmount);

                if (nextAmount <= 20) {
                    p.setFreezeTicks(400);
                    p.sendActionBar(ColorUtils.chat(String.format("&4&l[ 빙결수치 경고 : %d%% ]", nextAmount)));

                    if (nextAmount == 0) {
                        p.damage(9.0);
                        p.playSound(p, Sound.BLOCK_BELL_USE, 1.0f, 0.4f);
                    } else {
						p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1.0f, 1.0f);
						p.getWorld().spawnParticle(Particle.SNOWFLAKE, p.getLocation(), 15, 0.5, 0.5, 0.5, 0.01);
                    }
                }
            }), 0L, 20 * 3);
    }

	public boolean isTaskRunning() {
		return this.task != null;
	}

    public void stopTask() {
        if (this.task == null) return;
        this.task.cancel();
        this.task = null;
    }
}
