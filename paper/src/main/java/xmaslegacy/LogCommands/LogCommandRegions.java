package xmaslegacy.LogCommands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Region.Region;
import xmaslegacy.Region.RegionManager;
import xmaslegacy.Utils.InfoLevel;
import xmaslegacy.Utils.InfoUtils;
import xmaslegacy.Utils.SubCommand;

import java.util.List;

public class LogCommandRegions implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 2) {
            OfflinePlayer of = Bukkit.getOfflinePlayer(args[1]);
            if (of.hasPlayedBefore()) {
                List<Region> regions = RegionManager.INSTANCE.getRegion(of.getUniqueId());
                if (regions.isEmpty()) {
                    player.sendMessage(ColorUtils.chat(Alert.RED + " 구역이 없습니다."));
                    return;
                }
                SendRegions(player, regions);
            } else {
                player.sendMessage(ColorUtils.chat(Alert.RED + " 유저가 존재하지 않습니다."));
                player.playSound(player, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
            }
        } else InfoUtils.infoMsg(InfoLevel.ERROR, player, "유효하지 않은 명령어입니다.");
    }

    private void SendRegions(Player p, List<Region> regions) {
        for (Region region : regions) {
            p.sendMessage(ColorUtils.chat("&8&l--------------------------------"));
            p.sendMessage(ColorUtils.chat("&6&lRegion ID : &f" + region.Id()));
            p.sendMessage(ColorUtils.chat("&eOwner : &f" + region.Id()));

            int x = region.getChunkX();
            int z = region.getChunkZ();
            String world = region.getWorld().getName();

            p.sendMessage(ColorUtils.chat(String.format("&eLocation : &7%s (%d, %d)", world, x, z)));

            String entry = region.isEntryAllowed() ? "&a허용" : "&c차단";
            String interact = region.isInteractionAllowed() ? "&a허용" : "&c차단";
            p.sendMessage(ColorUtils.chat(String.format("&eSettings : &f출입[%s&f] 상호작용[%s&f]", entry, interact)));
        }
        p.sendMessage(ColorUtils.chat("&8&l--------------------------------"));
    }
}
