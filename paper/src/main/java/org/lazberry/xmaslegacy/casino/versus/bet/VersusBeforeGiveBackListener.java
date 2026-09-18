package org.lazberry.xmaslegacy.casino.versus.bet;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Listeners;
import org.lazberry.xmaslegacy.casino.versus.GameResult;
import org.lazberry.xmaslegacy.casino.versus.event.VersusResetEvent;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

@Listeners
@Registry.Include(type = ServerType.MAIN)
public class VersusBeforeGiveBackListener implements Listener {
	private final VersusBetManager vbm;

	@Inject
	public VersusBeforeGiveBackListener(VersusBetManager vbm) {
		this.vbm = vbm;
	}

	@EventHandler
	public void whenReset(VersusResetEvent e) {
		var result = e.getResult();
		if (result == GameResult.ERROR) {
			vbm.getValidBetPlayers().forEach(p -> {
				InfoUtils.error(p, "매치가 무효화 되었습니다. 베팅한 코인이 모두 반환되었습니다.");
				InfoUtils.warn(p, "코인이 반환되지 않았다면 '/가방'을 확인하거나, 문의하세요.");
			});
			vbm.giveBack();
			vbm.clearBets();
		}
		else if (result == GameResult.DRAW) {
			vbm.getValidBetPlayers().forEach(p -> {
				InfoUtils.error(p, "매치가 &6무승부&f로 종료되었습니다. 베팅한 코인의 &650%&f가 반환됩니다.");
				InfoUtils.warn(p, "코인이 반환되지 않았다면 '/가방'을 확인하거나, 문의하세요.");
			});
			//vbm.coinApplyProcessByResult(result);
			vbm.clearBets();
		}
	}
}
