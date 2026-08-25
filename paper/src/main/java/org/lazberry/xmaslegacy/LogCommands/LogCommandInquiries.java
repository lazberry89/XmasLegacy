package org.lazberry.xmaslegacy.LogCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.inquiry.InquiryManager;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.Map;
import java.util.UUID;

public class LogCommandInquiries implements SubCommand {
	private final @NotNull InquiryManager im;

	public LogCommandInquiries(@NotNull InquiryManager im) {
		this.im = im;
	}

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length == 1) {
            player.sendMessage(ColorUtils.chat("&b&l[현재 대기 중인 문의 목록]"));

            if (im.getActiveInquiries().isEmpty()) {
                player.sendMessage(ColorUtils.chat("&7대기 중인 문의가 없습니다. 평화롭네요!"));
                return;
            }

            for (Map.Entry<UUID, String> entry : im.getActiveInquiries().entrySet()) {
                String userName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                String msg = entry.getValue();

                Component comp = ColorUtils.chat(String.format("&e- &f%s &7: %s ", userName, msg))
                        .append(ColorUtils.chat("&a&l[이동]"))
                        .clickEvent(ClickEvent.runCommand("/이동문의 " + userName));

                player.sendMessage(comp);
            }
        }
    }
}
