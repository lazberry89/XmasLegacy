package org.lazberry.xmaslegacy.chat.whisper.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.chat.whisper.Whisper;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public class WhisperCommandUnmute implements SubCommand {
    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        Whisper.unmute(player);
        InfoUtils.info(player, "귓말을 허용했습니다. 귓말 알림이 활성화됩니다.");
    }
}
