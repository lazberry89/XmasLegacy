package xmaslegacy.RuleCommands;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.RuleManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

public class RuleCommandRemove implements SubCommand {
    private final @NotNull RuleManager rm;

    public RuleCommandRemove() {
        this.rm = RuleManager.INSTANCE;
    }

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 2) {
            if (rm.getBadWordList().contains(args[1])) {
                rm.removeBadWordList(args[1]);
                player.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, 0.5f, 1.0f);
            } else {
                player.sendMessage(ColorUtils.chat(Alert.RED + " 존재하지 않는 항목입니다."));
                player.playSound(player, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
            }
        } else InfoUtils.infoMsg(InfoLevel.ERROR, player, "유효하지 않은 명령어입니다.");
    }
}
