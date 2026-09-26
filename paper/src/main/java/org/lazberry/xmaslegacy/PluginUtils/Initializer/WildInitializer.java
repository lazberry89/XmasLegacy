package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Slf4j
@Registry.Include(type = ServerType.GLOBAL)
public class WildInitializer implements ServerInitializer {

	@Override
	public void initiate(@NotNull XmasLegacy plugin) {
		log.warn("Wild 모드로 시작합니다.");
		log.warn("server-type = \"wild\" 일치하지 않을 시에 config.yml을 수정하세요.");
	}
}
