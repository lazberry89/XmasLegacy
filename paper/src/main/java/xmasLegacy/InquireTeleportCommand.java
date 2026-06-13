package xmasLegacy;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Inquiry.InquiryManager;
import org.lazberry.xmaslegacy.Inquiry.InquiryStatus;

@Commands(command = "이동문의")
public class InquireTeleportCommand implements CommandExecutor {
	private final @NotNull InquiryManager im;
	private final @NotNull XmasLegacy plugin;

	public InquireTeleportCommand() {
		this.im = InquiryManager.INSTANCE;
		this.plugin = XmasLegacy.getInstance();
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull ...args) {
		if (!(commandSender instanceof Player p)) return false;
		if (!p.isOp()) {
			plugin.infoMsg(InfoLevel.ERROR, p, "관리자용 명령어에요!");
			return true;
		}

		if (args.length == 1) {
			Player target = Bukkit.getPlayerExact(args[0]);
			if (target != null && target.isOnline()) {
				if (im.hasInquiry(target.getUniqueId())) {
					p.teleport(target);
					p.spawnParticle(Particle.FLAME, p.getLocation().add(0, 1, 0), 15, 0.5, 1.2, 0.5, 0.01);
					plugin.infoMsg(InfoLevel.WARN, p, "이동하였습니다.");
					p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);

					plugin.infoMsg(InfoLevel.INFO, target, "관리자가 배정되었습니다 : &c&l" + p.getName());
                    target.playSound(target, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    im.updateInquiryStatus(target.getUniqueId(), InquiryStatus.RESOLVED);

					im.removeInquiry(target.getUniqueId());
				} else plugin.infoMsg(InfoLevel.ERROR, p, "해당 유저의 문의가 처리되었거나 없습니다.");
			}
		}
		return true;
	}
}
