package org.lazberry.xmaslegacy.mining.commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.mining.MineField;
import org.lazberry.xmaslegacy.mining.logics.MineCreateManager;
import org.lazberry.xmaslegacy.mining.logics.MineManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.UUID;

public record MineCommandCreate(MineCreateManager mcm, MineManager mm) implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 2) {
            String div = args[1];
            UUID uuid = player.getUniqueId();

            Location loc1 = mcm.getFirstSelection(uuid);
            Location loc2 = mcm.getSecondSelection(uuid);
            if (loc1 == null || loc2 == null) {
                InfoUtils.error(player, "Location settings are not done. Select all sides.");
                return;
            }
            MineField field = new MineField(loc1, loc2);

            switch (div.toLowerCase()) {
                case "internal" -> {
                    boolean result = mm.registerInternal(field);
                    if (result) {
                        InfoUtils.info(player, "Internal field is set.");
                        mcm.clearSelection(uuid);
                    } else {
                        InfoUtils.error(player, "Internal field should be smaller than External field.");
                    }
                }
                case "external" -> {
                    boolean result = mm.registerExternal(field);
                    if (result) {
                        InfoUtils.info(player, "External field is set.");
                        mcm.clearSelection(uuid);
                    } else {
                        InfoUtils.error(player, "External field should be larger than Internal field.");
                    }
                }
                default -> InfoUtils.error(player, "Select between 'internal' or 'external'");
            }
        } else InfoUtils.error(player, "Wrong usage of command");
    }
}
