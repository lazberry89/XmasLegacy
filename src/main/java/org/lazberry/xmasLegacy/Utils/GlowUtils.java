package org.lazberry.xmasLegacy.Utils;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class GlowUtils {
	public static void setGlowColor(Entity entity, NamedTextColor color) {
		// 1. 서버의 메인 스코어보드를 가져옵니다.
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

		// 2. 색상 이름을 따서 팀 이름을 만듭니다 (예: glow_red)
		String teamName = "glow_" + color.toString();
		Team team = scoreboard.getTeam(teamName);

		// 3. 해당 색상의 팀이 없으면 새로 만듭니다.
		if (team == null) {
			team = scoreboard.registerNewTeam(teamName);
			team.color(color); // 여기가 실제 테두리 색상을 결정하는 부분!
		}

		// 4. 엔티티를 팀에 추가하고 발광을 켭니다.
		team.addEntity(entity);
		entity.setGlowing(true);
	}
}
