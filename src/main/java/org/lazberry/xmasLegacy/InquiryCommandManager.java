package org.lazberry.xmasLegacy;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmasLegacy.Utils.ColorUtils;

@SuppressWarnings("ClassCanBeRecord")
public class InquiryCommandManager implements CommandExecutor {
	private final InquiryManager IM;

	public InquiryCommandManager(InquiryManager IM) {
		this.IM = IM;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
		if (!(commandSender instanceof Player p)) return false;
		if (IM.isCooldown(p)) return true;

		if (args.length == 0) {
			IM.Inquiry(p.getUniqueId(), null);
			p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 운영자를 호출했습니다. 조금만 기다려주세요!"));
			p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			return true;
		} else {
			String content = String.join(" ", args);
			IM.Inquiry(p.getUniqueId(), content);
			p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 문의가 접수되었습니다."));
			p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			return true;
		}

	}
}

