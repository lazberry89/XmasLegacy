package xmasLegacy.UserInfo;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Alert;
import xmasLegacy.XmasLegacy;

public class ScoreBoardManager {
	private final XmasLegacy plugin;
	private final UserManager um;
	private final Scoreboard scoreboard;
	private final Objective objective;
	private final User user;

	public ScoreBoardManager(XmasLegacy plugin, Player p) {
		this.plugin = plugin;
		this.um = UserManager.INSTANCE;
		this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		this.objective = scoreboard.registerNewObjective("xmas_board", Criteria.DUMMY, ColorUtils.chat(Alert.XmasLegacy.toString()));
		this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.user = um.getUser(p.getUniqueId());
		if (user == null) return;

		// 2. 초기 라인 세팅 (위에서 아래로 높은 점수 부여)
		setupLines(p.getName());

		p.setScoreboard(this.scoreboard);
	}

	private void setupLines(String playerName) {
		// 구분선 (고정) - 스타일 적용
		addStaticLine(Component.text("━━━━━━━━━━━━━━━━━━").color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.STRIKETHROUGH), 10);

		// 이름 (고정) - 굵게 + 색상
		addStaticLine(Component.text("닉네임: ").color(NamedTextColor.WHITE).decorate(TextDecoration.BOLD)
			.append(Component.text(playerName).color(NamedTextColor.AQUA).decorate(TextDecoration.ITALIC)), 9);

		// 실시간 변동 데이터 (Team 활용) - 다양한 색상과 스타일
		createTeam("job", Component.text("직업: ").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD),
			Component.text(this.user.getRole().getKor()).color(NamedTextColor.GOLD), 8);
		createTeam("money", Component.text("💰 돈: ").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD),
			Component.text(user.getDollars() + "$").color(NamedTextColor.GREEN), 7);
		createTeam("exp", Component.text("⭐ 경험치: ").color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD),
			Component.text("0").color(NamedTextColor.BLUE), 6);

		addStaticLine(Component.text(" "), 5); // 공백 한 줄

		createTeam("playtime", Component.text("⏰ 플레이타임: ").color(NamedTextColor.LIGHT_PURPLE).decorate(TextDecoration.BOLD),
			Component.text(user.getPlayTime() + "분").color(NamedTextColor.LIGHT_PURPLE), 4);

		addStaticLine(Component.text("━━━━━━━━━━━━━━━━━━").color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.STRIKETHROUGH), 3);
		addStaticLine(Component.text("🌟 XmasLegacy.aruru.kr").color(NamedTextColor.GOLD).decorate(TextDecoration.ITALIC), 2);
	}

	// 데이터 업데이트 메서드들
	public void updateInfo(String job, double money, int exp, String playtime) {
		updateTeamSuffix("job", Component.text(job));
		updateTeamSuffix("money", Component.text((int)money + "원"));
		updateTeamSuffix("exp", Component.text(String.valueOf(exp)));
		updateTeamSuffix("playtime", Component.text(playtime));
	}

	private void addStaticLine(Component text, int score) {
		objective.getScore(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(text)).setScore(score);
	}

	private void createTeam(String teamId, Component prefix, Component suffix, int score) {
		Team team = scoreboard.registerNewTeam(teamId);
		String entry = "team_entry_" + score;
		team.addEntry(entry);
		team.prefix(prefix);
		team.suffix(suffix);
		objective.getScore(entry).setScore(score);
	}

	private void updateTeamSuffix(String teamId, Component suffix) {
		Team team = scoreboard.getTeam(teamId);
		if (team != null) team.suffix(suffix);
	}
}
