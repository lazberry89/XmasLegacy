package org.lazberry.xmaslegacy.stock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
import org.lazberry.xmaslegacy.stock.display.StockDisplayManager;

@Task
@Slf4j
@Registry.Include(type = ServerType.MAIN)
public class StockMarketTask implements Tasks {
	private final StockManager sm;
	private final StockConfig sc;
	private final StockDisplayManager sdm;
	private @Nullable BukkitTask task;
	private @Getter boolean isOpen = false;

	@Inject
	public StockMarketTask(StockManager sm, StockConfig sc, StockDisplayManager sdm) {
		this.sm = sm;
        this.sc = sc;
		this.sdm = sdm;
    }

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			World world = Bukkit.getWorld(sc.getTargetWorldName());
			if (world == null) return;

			long currentTime = world.getTime();

			if (currentTime >= sc.getMinimumStartTime() && currentTime < sc.getMaximumStartTime()) {
				sdm.updateAll();
				sm.updateAllPrices();
				if (!isOpen) {
					isOpen = true;
					sm.setOpen(true);
					sc.saveStocks().whenComplete((v, e) -> {
						if (e == null) log.info("Stock info successfully saved in Scheduler.");
						else log.error("Failed to save info in Scheduler.", e);
					});

					String hoursNotice = " &7(" + sc.getOperatingHoursFormatted() + ")";
					Bukkit.broadcast(sm.icon().append(ColorUtils.chat(" &a주식 시장이 &a개장&f되었습니다! 주가가 새로 갱신되었습니다." + hoursNotice)));
				}
			} else {
				if (isOpen) {
					isOpen = false;
					sm.setOpen(false);
					Bukkit.broadcast(sm.icon().append(ColorUtils.chat(" 주식 시장이 &c마감&f되었습니다. 다음 날 아침에 개장됩니다.")));
				}
			}
		}, 0L, sc.getSchedulerInterval());
	}

	@Override
	public void stopTask() {
		if (this.task != null) {
			this.task.cancel();
			this.task = null;
		}
	}
}
