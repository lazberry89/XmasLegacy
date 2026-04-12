package org.lazberry.xmasLegacy;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmasLegacy.Utils.ColorUtils;

public class InquireTeleportCommand implements CommandExecutor {
	private final InquiryManager IM;

	public InquireTeleportCommand(InquiryManager IM) {
		this.IM = IM;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
		if (!(commandSender instanceof Player p)) return false;
		if (!p.isOp()) return false;

		if (args.length == 1) {
			Player target = Bukkit.getPlayerExact(args[0]);
			if (target != null && target.isOnline()) {
				if (IM.hasInquiry(target.getUniqueId())) {
					p.teleport(target);
					p.spawnParticle(Particle.FLAME, p.getLocation().add(0, 1, 0), 5, 0.5, 1.2, 0.5, 0.01);
					p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 이동하였습니다"));
					p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
					target.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 관리자가 이동하였습니다"));

					IM.removeInquiry(target.getUniqueId());
				} else {
					p.sendMessage(ColorUtils.chat(Prefix.RED + " 해당 유저의 문의가 처리되었거나 없습니다!"));
				}
			}
		}
		return true;
	}
}
