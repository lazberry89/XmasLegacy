package org.lazberry.xmaslegacy.PluginUtils.Initializer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;

@Slf4j
@Registry
public class HuntingInitializer implements ServerInitializer {

    @Override
    public void initiate(@NotNull XmasLegacy plugin) {
        log.warn("Hunting 모드로 시작합니다.");
        log.warn("server-type = \"hunting\" 일치하지 않을 시에 config.yml을 수정하세요.");
    }

    @Override
    public void shutdown(@NotNull XmasLegacy plugin) {
        ServerInitializer.super.shutdown(plugin);
    }
}
