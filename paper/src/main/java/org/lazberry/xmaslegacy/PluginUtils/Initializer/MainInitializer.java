package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.HuntingZone.HuntingZoneManager;
import org.lazberry.xmaslegacy.PlayerUtils.BagManager;
import org.lazberry.xmaslegacy.RoleSelection.RoleViewDesign;
import org.lazberry.xmaslegacy.User.UserSaveManager;
import org.lazberry.xmaslegacy.XmasLegacy;

@Slf4j
public class MainInitializer implements ServerInitializer {

	@Override
	public void enable(@NotNull XmasLegacy plugin) {
		log.warn("Main 모드로 시작합니다.");
		log.warn("server-type = \"main\" 일치하지 않을 시에 config.yml을 수정하세요.");
		RoleViewDesign.INSTANCE.init();
		BagManager.INSTANCE.loadAllBags();
		HuntingZoneManager.INSTANCE.init();
	}

	@Override
	public void disable(@NotNull XmasLegacy plugin) {
		BagManager.INSTANCE.saveAllBags();
		UserSaveManager.INSTANCE.saveAll();
	}
}
