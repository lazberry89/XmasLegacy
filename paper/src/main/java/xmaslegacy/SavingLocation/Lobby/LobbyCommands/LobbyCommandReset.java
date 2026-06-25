package xmaslegacy.SavingLocation.Lobby.LobbyCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import xmaslegacy.SavingLocation.Lobby.LobbyManager;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

public class LobbyCommandReset implements SubCommand {
    private final @NotNull LobbyManager lbm;

    public LobbyCommandReset(@NotNull LobbyManager lbm) {
        this.lbm = lbm;
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length == 1) {
            lbm.resetSpawn();
            InfoUtils.info(player, "성공적으로 위치가 초기화 되었습니다.");
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
