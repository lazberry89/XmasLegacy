package org.lazberry.xmaslegacy.casino.versus.bet;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Listeners;
import org.lazberry.xmaslegacy.casino.versus.event.VersusResetEvent;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;

@Listeners
@Components.Include(type = ServerType.MAIN)
public class VersusResultListener implements Listener {
	private final VersusBetManager vbm;

	@Inject
	public VersusResultListener(VersusBetManager vbm) {
		this.vbm = vbm;
	}

	@EventHandler
	public void whenReset(VersusResetEvent e) {
		var result = e.getResult();
		var winner = e.getWinner();

		vbm.coinApplyProcessByResult(result, winner);
	}
}
