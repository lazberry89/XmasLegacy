package org.lazberry.xmaslegacy.playerUtils.knockout;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Optional;

@Task
@Registry.Include(type = ServerType.GLOBAL)
public class KnockoutTask implements Tasks {
	private final Particle.DustTransition trans;
	private final KnockoutManager km;
	private volatile BukkitTask task;

	@Inject
	public KnockoutTask(KnockoutManager km) {
		trans = new Particle.DustTransition(Color.RED, Color.GRAY, 0.5f);
		this.km = km;
	}

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		if (task == null) synchronized (this) {
			if (task == null) {
				task = Bukkit.getScheduler().runTaskTimer(plugin, () ->
					km.getKnockoutPlayers().stream()
							.map(KnockoutPlayer::getPlayer)
							.filter(Optional::isPresent)
							.map(Optional::get)
							.filter(Player::isValid)
							.filter(Player::isOnline)
							.forEach(player -> {
								player.setPose(Pose.SWIMMING, true);
								player.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION,
										player.getLocation(), 2, 0.2, 0.2, 0.2, 0.01, trans);
							}), 0L, 10L);
			}
		}
	}

	@Override
	public void stopTask() {
		if (task != null) synchronized (this) {
			if (task != null) {
				task.cancel();
				task = null;
			}
		}
	}
}
