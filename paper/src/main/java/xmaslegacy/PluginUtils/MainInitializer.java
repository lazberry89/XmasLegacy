package xmaslegacy.PluginUtils;

import org.jetbrains.annotations.NotNull;
import xmaslegacy.Env.ConsumableManager;
import xmaslegacy.HuntingZone.CustomMobs.MobRepository;
import xmaslegacy.HuntingZone.HuntingZoneManager;
import xmaslegacy.HuntingZone.MobSpawnManager;
import xmaslegacy.PlayerUtils.BagManager;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.RoleSelection.RoleViewDesign;
import xmaslegacy.TransferPortal.PortalManager;
import xmaslegacy.XmasLegacy;

public class MainInitializer implements ServerInitializer {

	@Override
	public void setup(@NotNull XmasLegacy plugin) {
		plugin.getSLF4JLogger().warn("Main 모드로 시작합니다.");
		plugin.getSLF4JLogger().warn("server-type = \"main\" 일치하지 않을 시에 config.yml을 수정하세요.");
		RoleViewDesign.INSTANCE.init();

		RegionManager.INSTANCE.startGlobalIndicatorTask();

		ConsumableManager.INSTANCE.runCookieTimer(plugin);
		BagManager.INSTANCE.loadAllBags();

		MobRepository.INSTANCE.init();

		HuntingZoneManager.INSTANCE.init();
		MobSpawnManager.INSTANCE.startTask();
		PortalManager.INSTANCE.startPortalScheduler();

		//UserTagManager.runTask();
	}
}
