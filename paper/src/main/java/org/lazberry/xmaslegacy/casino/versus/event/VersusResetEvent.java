package org.lazberry.xmaslegacy.casino.versus.event;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.casino.versus.GameResult;

import java.util.UUID;

@Getter
public class VersusResetEvent extends Event {
    private static final HandlerList handler = new HandlerList();
    private final UUID blueTeamFighter;
    private final UUID redTeamFighter;
    private final GameResult result;

    public VersusResetEvent(UUID blueTeamFighter, UUID redTeamFighter, GameResult result) {
        this.blueTeamFighter = blueTeamFighter;
        this.redTeamFighter = redTeamFighter;
        this.result = result;
    }

    public static HandlerList getHandlerList() {
        return handler;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;
    }
}
