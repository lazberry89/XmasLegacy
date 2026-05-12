package xmasLegacy.Gacha;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;

public class GachaCommand implements CommandExecutor, TabCompleter {
    private final XmasLegacy plugin;
    private final GachaManager gm;

    public GachaCommand(XmasLegacy plugin) {
        this.plugin = plugin;
        this.gm = plugin.GM;
    }

    //         0    1           2     3     4
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) return true;

        // 1. 최소 인자 수 체크
        if (args.length >= 5) {
            String action = args[0].toLowerCase();
            String key = args[1];
            GachaGrade grade;
            double chance;
            List<BundleType> types = new ArrayList<>();

            try {
                grade = GachaGrade.valueOf(args[2].toUpperCase());
                chance = Double.parseDouble(args[3]);
                for (int i = 4; i < args.length; i++) {
                    types.add(BundleType.valueOf(args[i].toUpperCase()));
                }
            } catch (IllegalArgumentException e) {
                error(p, "인자 값이 올바르지 않습니다. (Enum 오타 또는 숫자 형식 오류)");
                return true;
            }

            // 3. 아이템 체크
            ItemStack item = p.getInventory().getItemInMainHand();
            if (item.getType().isAir()) {
                error(p, "손에 아이템을 들고 있어야 합니다!");
                return true;
            }

            // 4. 액션 처리
            if (args[0].equalsIgnoreCase("add")) {
                // GM에 types 리스트를 배열로 변환해서 한 번에 전달 (가변인자 활용)
                gm.addGacha(key, item, grade, chance, types.toArray(new BundleType[0]));
                p.sendMessage(ColorUtils.chat(Prefix.GREEN + " 가챠 아이템이 성공적으로 등록되었습니다: " + key));
                p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

            } else {
                error(p, "알 수 없는 작업입니다: " + action);
            }
        } else if (args.length == 3) {
            String action = args[0].toLowerCase();
            String key = args[1];
            if (action.equalsIgnoreCase("remove")) {
                try {
                    BundleType type = BundleType.valueOf(args[2]);
                    if (gm.removeGacha(key, type)) {
                        p.sendMessage(ColorUtils.chat(Prefix.GREEN + " 아이템을 삭제하였습니다."));
                        p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
                    } else {
                        error(p, "존재하지 않는 아이템이에요!");
                    }
                } catch (IllegalArgumentException e) {
                    error(p, "유효하지 않은 타입이에요!");
                    return true;
                }
            }
        } else {
            error(p, "사용법: /gacha <add | remove> <이름> <등급> <확률> <번들타입...>");
            return true;
        }
        return true;
    }


    private void error(Player p, String msg) {
        p.sendMessage(ColorUtils.chat(Prefix.RED + " " + msg));
        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of();
    }
}
