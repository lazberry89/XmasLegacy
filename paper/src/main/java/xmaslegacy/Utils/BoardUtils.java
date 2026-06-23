package xmaslegacy.Utils;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class BoardUtils {
    private final @NotNull @Getter Scoreboard scoreboard;
    private final @NotNull Objective objective;
    private final @NotNull @Getter Player player;
    private final @NotNull Map<Integer, Team> lines = new HashMap<>(20);

    public static @NotNull BoardUtils create(@NotNull Player player, @NotNull Component title, @NotNull Consumer<BoardUtils> setup) {
        BoardUtils board = new BoardUtils(player, title);
        setup.accept(board);
        return board;
    }

    public void edit(@NotNull Consumer<BoardUtils> action) {
        action.accept(this);
    }

    /**
     * 새로운 스코어보드를 생성하고 플레이어에게 적용합니다.
     * @param player 스코어보드를 띄울 유저
     * @param title 최상단 제목 (Component)
     */
    @ApiStatus.Internal
    private BoardUtils(@NotNull Player player, @NotNull Component title) {
        this.player = player;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective("party_board", Criteria.DUMMY, title);
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = 0; i < 15; i++) {
            Team team = scoreboard.registerNewTeam("line_" + i);
            String invisibleEntry = getInvisibleEntry(i);
            team.addEntry(invisibleEntry);
            this.lines.put(i, team);
        }

        player.setScoreboard(this.scoreboard);
    }

    public void updateTitle(@NotNull Component title) {
        this.objective.displayName(title);
    }

    /**
     * 특정 줄(0~14)의 내용을 Component로 업데이트합니다.
     * @param line 줄 번호 (0이 맨 위쪽)
     * @param text 들어갈 텍스트 (Component)
     */
    public void setLine(int line, @NotNull Component text) {
        if (line < 0 || line > 14) return;

        Team team = lines.get(line);
        if (team != null) {
            team.prefix(text);

            String invisibleEntry = getInvisibleEntry(line);
            this.objective.getScore(invisibleEntry).setScore(15 - line);
        }
    }

    /**
     * 특정 줄을 화면에서 지웁니다.
     * line 1 ~ 14
     */
    public void removeLine(int line) {
        if (line < 0 || line > 14) return;
        String invisibleEntry = getInvisibleEntry(line);
        this.scoreboard.resetScores(invisibleEntry);
    }

    private @NotNull String getInvisibleEntry(int line) {
        return "§" + Integer.toHexString(line) + "§r";
    }
}
