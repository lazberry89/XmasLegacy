package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.Env.ConsumableManager;
import org.lazberry.xmaslegacy.HuntingZone.CustomMobs.MobRepository;
import org.lazberry.xmaslegacy.HuntingZone.HuntingZoneManager;
import org.lazberry.xmaslegacy.HuntingZone.MobSpawnManager;
import org.lazberry.xmaslegacy.PartyScoreBoard.UserPartyScoreBoard;
import org.lazberry.xmaslegacy.PlayerUtils.BagManager;
import org.lazberry.xmaslegacy.Region.RegionManager;
import org.lazberry.xmaslegacy.RoleSelection.RoleViewDesign;
import org.lazberry.xmaslegacy.TransferPortal.PortalManager;
import org.lazberry.xmaslegacy.XmasLegacy;

@Slf4j
public class MainInitializer implements ServerInitializer {

	@Override
	public void enable(@NotNull XmasLegacy plugin) {
		log.warn("Main 모드로 시작합니다.");
		log.warn("server-type = \"main\" 일치하지 않을 시에 config.yml을 수정하세요.");
		RoleViewDesign.INSTANCE.init();

		UserPartyScoreBoard.INSTANCE.startTask();

		RegionManager.INSTANCE.startGlobalIndicatorTask();

		ConsumableManager.INSTANCE.runCookieTimer(plugin);
		BagManager.INSTANCE.loadAllBags();

		MobRepository.INSTANCE.init();

		HuntingZoneManager.INSTANCE.init();
		MobSpawnManager.INSTANCE.startTask();
		PortalManager.INSTANCE.startPortalScheduler();

		plugin.registerMainReflection();

		//UserTagManager.runTask();
	}

	@Override
	public void disable(@NotNull XmasLegacy plugin) {
		UserPartyScoreBoard.INSTANCE.stopTask();
		ConsumableManager.INSTANCE.stopCookieTimer();
		BagManager.INSTANCE.saveAllBags();
		MobSpawnManager.INSTANCE.stopTask();
		PortalManager.INSTANCE.startPortalScheduler();

		UserManager.INSTANCE.saveAll();
	}
}
