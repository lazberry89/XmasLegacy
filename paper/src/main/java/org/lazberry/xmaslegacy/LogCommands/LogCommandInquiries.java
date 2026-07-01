package org.lazberry.xmaslegacy.LogCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Inquiry.InquiryManager;
import org.lazberry.xmaslegacy.Utils.SubCommand;

import java.util.Map;
import java.util.UUID;

public class LogCommandInquiries implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        var im = InquiryManager.INSTANCE;
        if (args.length == 1) {
            player.sendMessage(ColorUtils.chat("&b&l[현재 대기 중인 문의 목록]"));

            if (im.getInquiryMap().isEmpty()) {
                player.sendMessage(ColorUtils.chat("&7대기 중인 문의가 없습니다. 평화롭네요!"));
                return;
            }

            for (Map.Entry<UUID, String> entry : im.getInquiryMap().entrySet()) {
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
