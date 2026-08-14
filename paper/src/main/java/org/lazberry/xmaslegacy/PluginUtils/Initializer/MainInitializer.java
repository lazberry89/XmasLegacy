package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.stock.display.StockDisplayManager;

@Slf4j
@Registry
public class MainInitializer implements ServerInitializer {
	private final StockDisplayManager sdm;

	@Inject
	public MainInitializer(StockDisplayManager sdm) {
		this.sdm = sdm;
	}

	@Override
	public void initiate(@NotNull XmasLegacy plugin) {
		log.warn("Main 모드로 시작합니다.");
		log.warn("server-type = \"main\" 일치하지 않을 시에 config.yml을 수정하세요.");
	}

	@Override
	public void shutdown(@NotNull XmasLegacy plugin) {
		sdm.clear();
	}
}
