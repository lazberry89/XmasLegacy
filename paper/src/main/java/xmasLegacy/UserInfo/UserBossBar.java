package xmasLegacy.UserInfo;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import xmasLegacy.XmasLegacy;

import java.util.Random;

public class UserBossBar {
	private BossBar bossBar;
	private BossBar.Color currentColor;
	private final Random random = new Random();

	public void createBossBar() {
		// 초기 색상 설정
		this.currentColor = BossBar.Color.RED;

		// 1. 보스바 구성 (제목, 진행도, 색상, 스타일)
		Component title = Component.text("❄ Xmas ", NamedTextColor.WHITE)
				.append(Component.text("Legacy", NamedTextColor.AQUA, TextDecoration.BOLD))
				.append(Component.text(" 서버에 오신 것을 환영합니다! ❄", NamedTextColor.WHITE));

		// BossBar.bossBar(제목, 진행도(0.0~1.0), 색상, 스타일)
		this.bossBar = BossBar.bossBar(title, 0.0f, currentColor, BossBar.Overlay.PROGRESS);
	}

	// 플레이어에게 보스바 표시
	public void show(Player player) {
		player.showBossBar(this.bossBar);
	}

	// 플레이어에게 보스바 제거
	public void hide(Player player) {
		player.hideBossBar(this.bossBar);
	}

	// 무지개 게이지 시작 (천천히 0~100% 채우고 색상 변경)
	public void startRainbowProgress() {
		Bukkit.getScheduler().runTaskTimer(JavaPlugin.getPlugin(XmasLegacy.class), task -> {
			float currentProgress = bossBar.progress();
			float newProgress = Math.min(currentProgress + 0.01f, 1.0f); // 1%씩 증가

			// 진행도에 따라 색상 변경 (무지개)
			BossBar.Color newColor = getRainbowColor(newProgress);
			if (!currentColor.equals(newColor)) {
				currentColor = newColor;
				bossBar.color(newColor);
			}

			bossBar.progress(newProgress);

			// 100% 도달 시 색상 랜덤 변경 후 다시 시작
			if (newProgress >= 1.0f) {
				changeToRandomColor();
				bossBar.progress(0.0f); // 다시 0부터 시작
			}
		}, 0L, 2L); // 매 2틱(0.1초)마다 실행
	}

	// 진행도에 따른 무지개 색상 반환
	private BossBar.Color getRainbowColor(float progress) {
		int colorIndex = (int) (progress * 6); // 0~5 범위
		return switch (colorIndex) {
			case 0 -> BossBar.Color.RED;
			case 1 -> BossBar.Color.YELLOW;
			case 2 -> BossBar.Color.GREEN;
			case 3 -> BossBar.Color.BLUE;
			case 4 -> BossBar.Color.PURPLE;
			default -> BossBar.Color.PINK;
		};
	}

	// 랜덤 색상으로 변경
	private void changeToRandomColor() {
		BossBar.Color[] colors = {BossBar.Color.RED, BossBar.Color.YELLOW, BossBar.Color.GREEN,
									BossBar.Color.BLUE, BossBar.Color.PURPLE, BossBar.Color.PINK};
		currentColor = colors[random.nextInt(colors.length)];
		bossBar.color(currentColor);
	}

	// 실시간 업데이트 (진행도 및 제목 변경)
	public void update(float progress, String message) {
		this.bossBar.progress(progress);
		this.bossBar.name(Component.text(message, NamedTextColor.YELLOW));
	}
}
