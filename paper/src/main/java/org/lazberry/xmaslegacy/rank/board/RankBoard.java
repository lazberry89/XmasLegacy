package org.lazberry.xmaslegacy.rank.board;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;
import org.lazberry.xmaslegacy.rank.RankType;

public record RankBoard(RankType rankType, Location location, int printLimit) {

    private Color getColorByRankType(RankType rankType) {
        return switch (rankType) {
            case DOLLAR -> Color.ORANGE;
            case EXP -> Color.GREEN;
            case ROLE_EXP -> Color.RED;
            case PLAYTIME -> Color.AQUA;
        };
    }

    public TextDisplay createDisplay() {
        return location.getWorld().spawn(location, TextDisplay.class, t -> {
            t.setGlowColorOverride(getColorByRankType(rankType));
            t.setGlowing(true);
            t.setAlignment(TextDisplay.TextAlignment.CENTER);
            t.text();
        });
    }
}
