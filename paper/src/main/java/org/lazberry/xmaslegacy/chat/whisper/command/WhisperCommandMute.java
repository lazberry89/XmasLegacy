package org.lazberry.xmaslegacy.chat.whisper.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.chat.whisper.Whisper;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public class WhisperCommandMute implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        Whisper.mute(player);
        InfoUtils.info(player, "귓말을 차단했습니다. 더이상 알림이 뜨지 않습니다.");
    }
}
