package xmaslegacy.Icing;

import lombok.extern.slf4j.Slf4j;
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
import xmaslegacy.Annotation.Task;
import xmaslegacy.PluginUtils.ServerType;
import xmaslegacy.PluginUtils.Tasks;
import xmaslegacy.XmasLegacy;

@Slf4j
@Task(type = ServerType.GLOBAL)
public enum IcingSystem implements Tasks {
    INSTANCE;

    private @Nullable BukkitTask task;

    IcingSystem() {}

	/**
	 * This scheduler reduces player's icing state.
	 * Built not to be stopped, check if stopped.
	 * @param plugin Plugin instance.
	 */
	@Override
    public void startTask(@NotNull XmasLegacy plugin) {
        if (this.task != null) return;
		log.warn("Basic Icing Scheduler started.");
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

                sendWarnAndAction(p, nextAmount);
            }), 0L, 20 * 3);
    }

	private void sendWarnAndAction(@NotNull Player player, int nextAmount) {
		if (nextAmount <= 20) {
			player.setFreezeTicks(400);
			player.sendActionBar(ColorUtils.chat(String.format("&4&l[ 빙결수치 경고 : %d%% ]", nextAmount)));

			if (nextAmount == 0) {
				player.damage(9.0);
				player.getWorld().playSound(player, Sound.BLOCK_BELL_USE, 1.0f, 0.4f);
			} else {
				//p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_HURT_FREEZE, 1.0f, 1.0f);
				player.getWorld().spawnParticle(Particle.SNOWFLAKE, player.getLocation(), 25, 0.7, 1, 0.7, 0.01);
			}
		}
	}

	/**
	 * Only called when Server is closing.
	 * Icing scheduler is built never to be stopped.
	 */
	@Override
    public void stopTask() {
        if (this.task == null) return;
        this.task.cancel();
        this.task = null;

		log.warn("Icing Scheduler stopped. Check if it's valid.");
    }
}
