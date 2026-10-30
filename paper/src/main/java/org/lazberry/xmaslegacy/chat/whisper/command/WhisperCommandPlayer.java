package org.lazberry.xmaslegacy.chat.whisper.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.JedisService;
import org.lazberry.xmaslegacy.chat.whisper.Whisper;
import org.lazberry.xmaslegacy.chat.whisper.WhisperManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.Arrays;
import java.util.stream.Collectors;

public record WhisperCommandPlayer(WhisperManager whisperManager) implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (Whisper.isMuted(player)) {
            InfoUtils.warn(player, "귓말이 차단 상태입니다. 전송할 수 없습니다.");
            return;
        }
        var uuid = player.getUniqueId();
        if (args.length >= 2) {
            var text = Arrays.stream(args)
                    .skip(1)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining(" "));
            var targetName = args[0];
            var result = whisperManager.sendWhisper(player,
                    JedisService.getUserServer(uuid), targetName, text);
            if (result) InfoUtils.info(player, "전송했습니다.");
            else InfoUtils.error(player, "해당 유저가 오프라인입니다.");
        } else InfoUtils.error(player, "내용을 적어주세요!");
    }
}
