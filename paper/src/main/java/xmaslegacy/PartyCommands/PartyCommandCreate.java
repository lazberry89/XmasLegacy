package xmaslegacy.PartyCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.ServerTransfer;
import xmaslegacy.Utils.SubCommand;

public class PartyCommandCreate implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var user = UserManager.INSTANCE.getUser(player.getUniqueId());
        if (user == null) {
            ServerTransfer.sendReloadNotice(player);
            return;
        }
        if (args.length >= 1) {
            if (PartyManager.INSTANCE.createParty(user)) InfoUtils.info(player, "파티가 생성되었습니다.");
            else InfoUtils.error(player, "파티를 생성하지 못했습니다. 이미 파티에 소속되어있습니다.");
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
