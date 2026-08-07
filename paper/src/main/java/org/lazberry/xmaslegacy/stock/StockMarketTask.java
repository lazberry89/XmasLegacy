package org.lazberry.xmaslegacy.stock;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Task;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Task
@Registry.Include(type = ServerType.GLOBAL)
public class StockMarketTask implements Tasks {
	private final StockManager sm;
	private @Nullable BukkitTask task;
	private @Getter boolean isOpen = false;
	private long lastCheckedTime = -1;

	@Inject
	public StockMarketTask(StockManager sm) {
		this.sm = sm;
	}

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			World world = Bukkit.getWorld("world");
			if (world == null) return;
			long currentTime = world.getTime();

			if (currentTime >= 0 && currentTime < 12000) {
				if (!isOpen) {
					isOpen = true;
					Bukkit.broadcast(sm.icon().append(ColorUtils.chat(" 주식 시장이 시작되었습니다! &7(06:00 ~ 18:00)")));
				}
			} else {
				if (isOpen) {
					isOpen = false;
					Bukkit.broadcast(sm.icon().append(ColorUtils.chat(" 주식 시장이 마감되었습니다. 다음 날 아침에 개장됩니다.")));
				}
			}

			boolean passedNight = (lastCheckedTime < 17000 && currentTime >= 17000);
			boolean skippedNight = (lastCheckedTime > currentTime && lastCheckedTime < 17000);

			if (passedNight || skippedNight) {
				sm.updateAllPrices();
				Bukkit.broadcast(sm.icon().append(ColorUtils.chat(" 주가가 새로 변동되었습니다!")));
			}

			lastCheckedTime = currentTime;
		}, 0L, 20L);
	}

	@Override
	public void stopTask() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
	}
}
