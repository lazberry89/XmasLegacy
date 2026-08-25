package org.lazberry.xmaslegacy.PartyCommands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;
import org.lazberry.xmaslegacy.utils.UserHandler;

public class PartyCommandCreate implements SubCommand {
	private final @NotNull UserManager um;
	private final @NotNull PartyManager pm;

	public PartyCommandCreate(@NotNull UserManager um, @NotNull PartyManager pm) {
		this.um = um;
		this.pm = pm;
	}

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var user = um.getUser(player.getUniqueId());
        if (user == null) {
            UserHandler.sendReloadNotice(player);
            return;
        }
        if (args.length >= 1) {
            if (pm.createParty(user)) InfoUtils.info(player, "파티가 생성되었습니다.");
            else InfoUtils.error(player, "파티를 생성하지 못했습니다. 이미 파티에 소속되어있습니다.");
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
