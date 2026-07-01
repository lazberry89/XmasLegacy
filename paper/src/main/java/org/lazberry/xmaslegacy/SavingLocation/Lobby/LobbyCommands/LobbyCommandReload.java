package org.lazberry.xmaslegacy.SavingLocation.Lobby.LobbyCommands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.SavingLocation.Lobby.LobbyManager;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;
import org.lazberry.xmaslegacy.XmasLegacy;

public class LobbyCommandReload implements SubCommand {
    private final @NotNull LobbyManager lbm;

    public LobbyCommandReload(@NotNull LobbyManager lbm) {
        this.lbm = lbm;
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length == 1) {
            InfoUtils.warn(player, "&7위치정보 불러오는중..");
            lbm.reload().thenAccept(success ->
                    Bukkit.getScheduler().runTask(XmasLegacy.getInstance(), () -> {
                        if (success) {
                            player.sendMessage(ColorUtils.chat(Alert.GREEN + " 위치 정보를 성공적으로 새로 로드했습니다!"));
                            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                        } else {
                            player.sendMessage(ColorUtils.chat(Alert.RED + " 로드 중 오류가 발생했거나 스폰 설정이 없습니다."));
                            player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                        }
                    }));
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
