package org.lazberry.xmaslegacy.SavingLocation.Lobby.LobbyCommands;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.SavingLocation.Lobby.LobbyManager;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.SubCommand;

public class LobbyCommandLocation implements SubCommand {
    private final @NotNull LobbyManager lbm;

    public LobbyCommandLocation(@NotNull LobbyManager lbm) {
        this.lbm = lbm;
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length == 1) {
            Location loc = lbm.getSpawn();
            if (loc == null) {
                player.sendMessage(ColorUtils.chat(Alert.RED + " 스폰위치가 설정되지 않았습니다."));
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                return;
            }
            player.sendMessage(ColorUtils.chat(String.format("%s 현재 로비 스폰위치 :&6 %.1f %.1f %.1f", Alert.YELLOW, loc.getX(), loc.getY(), loc.getZ())));
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
