package xmaslegacy.SavingLocation.Lobby.LobbyCommands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.SavingLocation.Lobby.LobbyManager;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;
import xmaslegacy.XmasLegacy;

public class LobbyCommandSet implements SubCommand {
    private final @NotNull LobbyManager lbm;

    public LobbyCommandSet(@NotNull LobbyManager lbm) {
        this.lbm = lbm;
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length == 1) {
            Location loc = player.getLocation();
            lbm.setSpawn(loc);

            player.sendMessage(ColorUtils.chat(Alert.YELLOW + " 위치 저장 중..."));
            lbm.save().thenRun(() ->
                    Bukkit.getScheduler().runTask(XmasLegacy.getInstance(), () -> {
                        player.sendMessage(ColorUtils.chat(String.format("%s 스폰 위치가 파일에 저장되었습니다!", Alert.GREEN)));
                        player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    })
            );
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
