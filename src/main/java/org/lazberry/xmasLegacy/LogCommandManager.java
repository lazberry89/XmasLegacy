package org.lazberry.xmasLegacy;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmasLegacy.Utils.ColorUtils;
import org.lazberry.xmasLegacy.Utils.ComponentChanger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LogCommandManager implements CommandExecutor, TabCompleter {
    private final InquiryManager IM;
    private final XmasLegacy plugin;

    public LogCommandManager(InquiryManager IM, XmasLegacy plugin) {
        this.IM = IM;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
        if (!(commandSender instanceof Player p)) return false;
        if (!p.isOp()) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 로그를 볼 수 있는 권한이 없습니다."));
            p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
            return true;
        }
        if (args.length == 2) {
            switch (args[0]) {
                case "inquiry" -> {
                    OfflinePlayer op = Bukkit.getOfflinePlayer(args[1]);
                    UUID uuid = op.getUniqueId();
                    IM.getInquiryLogs(uuid).forEach(p::sendMessage);
                }
                case "inquiries" -> {
                    p.sendMessage(ColorUtils.chat("&b&l[현재 대기 중인 문의 목록]"));

                    if (IM.getInquiryMap().isEmpty()) {
                        p.sendMessage(ColorUtils.chat("&7대기 중인 문의가 없습니다. 평화롭네요!"));
                        return true;
                    }

                    for (Map.Entry<UUID, String> entry : IM.getInquiryMap().entrySet()) {
                        String userName = Bukkit.getOfflinePlayer(entry.getKey()).getName();
                        String msg = entry.getValue();

                        // 채팅창에 클릭 가능한 메시지로 띄워줌
                        Component comp = ComponentChanger.comp("&e- &f" + userName + " &7: " + msg + " ")
                                .append(ComponentChanger.comp("&a&l[이동]"))
                                .clickEvent(ClickEvent.runCommand("/이동문의 " + userName));

                        p.sendMessage(comp);
                    }
                }
                default -> {
                    p.sendMessage(ColorUtils.chat(Prefix.RED + " 유효한 명령어가 아닙니다."));
                    p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String... args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("inquiry");
        }
        return completions;
    }
}
