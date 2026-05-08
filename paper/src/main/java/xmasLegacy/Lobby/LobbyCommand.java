package xmasLegacy.Lobby;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;

public class LobbyCommand implements CommandExecutor, TabCompleter {
    private final LobbyManager LBM;
    private final XmasLegacy plugin;

    public LobbyCommand(LobbyManager LBM, XmasLegacy plugin) {
        this.LBM = LBM;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) return true;
        if (!p.isOp()) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 죄송한데, 관리자용 명령어에요!"));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return true;
        }
        if (!plugin.getServerType().equals("lobby")) {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 해당 서버에서는 사용할 수 없는 명령어에요!"));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return true;
        }
        if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "set" -> {
                    Location loc = p.getLocation();
                    LBM.setSpawn(loc);

                    LBM.save().thenRun(() ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            p.sendMessage(ColorUtils.chat(String.format("%s 스폰 위치가 파일에 저장되었습니다!", Prefix.GREEN)));
                            p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        })
                    );
                    p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 위치 저장 중..."));
                }
                case "location" -> {
                    Location loc = LBM.getSpawn();
                    if (loc == null) {
                        p.sendMessage(ColorUtils.chat(Prefix.RED + " 스폰위치가 설정되지 않았습니다."));
                        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                        return true;
                    }
                    p.sendMessage(ColorUtils.chat(String.format("%s 현재 로비 스폰위치 :&6 %.1f %.1f %.1f", Prefix.YELLOW, loc.getX(), loc.getY(), loc.getZ())));
                }
                case "reset" -> {
                    LBM.resetSpawn();
                    p.sendMessage(Prefix.GREEN + " 성공적으로 위치가 초기화되었습니다.");
                }
                case "reload" -> {
                    p.sendMessage(ColorUtils.chat(Prefix.YELLOW + " 위치 정보를 새로 불러오는 중..."));

                    LBM.reload().thenAccept(success -> {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (success) {
                                p.sendMessage(ColorUtils.chat(Prefix.GREEN + " 위치 정보를 성공적으로 새로 로드했습니다!"));
                                p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
                            } else {
                                p.sendMessage(ColorUtils.chat(Prefix.RED + " 로드 중 오류가 발생했거나 스폰 설정이 없습니다."));
                                p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                            }
                            plugin.playConsoleSound();
                        });
                    });
                }
                default -> {
                    p.sendMessage(ColorUtils.chat(Prefix.RED + " 올바른 명령어 사용법이 아닙니다."));
                    p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                    return false;
                }
            }
        } else {
            p.sendMessage(ColorUtils.chat(Prefix.RED + " 올바른 명령어 사용법이 아닙니다."));
            p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
            return false;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.add("set");
            result.add("location");
            result.add("reset");
            result.add("reload");
        }
        return result;
    }
}
