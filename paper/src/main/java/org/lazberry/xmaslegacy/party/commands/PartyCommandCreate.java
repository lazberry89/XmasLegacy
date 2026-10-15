package org.lazberry.xmaslegacy.party.commands;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

public class PartyCommandCreate implements SubCommand {
	private final PartyManager pm;

	public PartyCommandCreate(PartyManager pm) {
		this.pm = pm;
	}

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var uuid = player.getUniqueId();
        if (args.length >= 1) {
            if (pm.createParty(uuid)) InfoUtils.info(player, "파티가 생성되었습니다.");
            else InfoUtils.error(player, "파티를 생성하지 못했습니다. 이미 파티에 소속되어있습니다.");
        } else InfoUtils.error(player, "유효하지 않은 명령어입니다.");
    }
}
