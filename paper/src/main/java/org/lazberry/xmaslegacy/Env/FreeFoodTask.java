package org.lazberry.xmaslegacy.Env;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Annotation.Task;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.PlayerUtils.BagManager;
import org.lazberry.xmaslegacy.PluginUtils.Initializers;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Manager;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Map;

@Slf4j
@Inject
@Task(type = ServerType.MAIN)
public class FreeFoodTask implements Tasks {
	private @Nullable BukkitTask task;
	private @Getter boolean isRunning = false;
	private @Manager @NotNull UserManager um;
	private @Manager @NotNull BagManager bm;

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		if (this.isRunning || this.task != null) return;
		this.isRunning = true;
		this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			for (Player p : Bukkit.getOnlinePlayers()) {
				var user = um.getUser(p.getUniqueId());
				if (user == null) continue;

				if (!user.ifWantsCookie()) continue;
				Map<Integer, ItemStack> leftOver = p.getInventory().addItem(Cookie.cookie(Constants.COOKIE_COUNT));

				if (!leftOver.isEmpty()) {
					leftOver.values().forEach(item -> bm.addItem(p, item));
				}
			}
			log.info("Cookies are given to every users. DURATION: {}m", Constants.COOKIE_TIMER_MINUTE);
		}, 20 * 60 * Constants.COOKIE_TIMER_MINUTE, 20 * 60 * Constants.COOKIE_TIMER_MINUTE);
	}

	@Override
	public void stopTask() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
		this.isRunning = false;
	}
}
