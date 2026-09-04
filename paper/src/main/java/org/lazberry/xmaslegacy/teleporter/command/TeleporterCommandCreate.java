package org.lazberry.xmaslegacy.teleporter.command;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.teleporter.logic.Teleporter;
import org.lazberry.xmaslegacy.teleporter.logic.TeleporterCreateManager;
import org.lazberry.xmaslegacy.teleporter.logic.TeleporterManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.SubCommand;

import java.util.UUID;

public record TeleporterCommandCreate(TeleporterManager tm, TeleporterCreateManager tcm) implements SubCommand {

    @Override
    public void execute(@NotNull Player player, @NotNull String @NotNull ... args) {
        if (args.length >= 2 ) {
            UUID uuid = player.getUniqueId();
            Location loc1 = tcm.getFirstLoc(uuid);
            Location loc2 = tcm.getSecondLoc(uuid);
            Location destination = tcm.getDestination(uuid);

            if (loc1 == null || loc2 == null || destination == null) {
                InfoUtils.error(player, "Location selection is not completely done. (Side1, Side2, Destination)");
                return;
            }
            String id = args[1];
            Teleporter teleporter = new Teleporter(loc1, loc2, Color.GRAY);
            if (tm.registerWay(id, teleporter, destination)) {
                InfoUtils.info(player, "Successfully linked portal to destination. &6(id: {})", id);
                tcm.clearSelection(uuid);
            } else {
                InfoUtils.error(player, "Id '{}' already exists.", id);
            }
        } else InfoUtils.error(player, "Not valid commands.");
    }
}
