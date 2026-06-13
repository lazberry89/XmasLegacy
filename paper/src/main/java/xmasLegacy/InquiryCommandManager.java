package xmasLegacy;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Inquiry.InquiryManager;
import org.lazberry.xmaslegacy.settings.Alert;

@Commands(command = "문의")
public class InquiryCommandManager implements CommandExecutor {
	private final InquiryManager im;

	public InquiryCommandManager() {
		this.im = InquiryManager.INSTANCE;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull...args) {
		if (!(commandSender instanceof Player p)) return true;
		if (im.checkAndSetCooldown(p.getUniqueId())) {
			long secondsLeft = im.getRemainingCooldown(p.getUniqueId());
			p.sendMessage(ColorUtils.chat(Alert.RED + " 아직 문의할 수 없습니다. (" + secondsLeft + "초 남음)"));
			return true;
		}

		if (args.length == 0) {
			p.sendMessage(im.Inquiry(p.getUniqueId(), null));
			p.sendMessage(ColorUtils.chat(Alert.GREEN + " 운영자를 호출했습니다. 조금만 기다려주세요!"));
		} else {
			String content = String.join(" ", args);
			p.sendMessage(im.Inquiry(p.getUniqueId(), content));
			p.sendMessage(ColorUtils.chat(Alert.GREEN + " 문의가 접수되었습니다."));
		}
		p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
		return true;
	}
}

