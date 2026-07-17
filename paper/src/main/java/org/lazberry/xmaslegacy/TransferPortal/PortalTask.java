package org.lazberry.xmaslegacy.TransferPortal;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Annotation.Task;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.Party;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.PluginUtils.Tasks;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.Utils.InfoUtils;
import org.lazberry.xmaslegacy.Utils.ServerTransfer;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.*;

@Slf4j
@Inject
@Task(type = ServerType.GLOBAL)
public class PortalTask implements Tasks {
	private @Nullable BukkitTask task;
	private @NotNull PortalManager pt;

	@Override
	public void startTask(@NotNull XmasLegacy plugin) {
		this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
			@NotNull Set<UUID> processedPlayers = new HashSet<>();
			var pm = PartyManager.INSTANCE;

			for (Player player : Bukkit.getOnlinePlayers()) {
				UUID pUUID = player.getUniqueId();
				if (processedPlayers.contains(pUUID)) continue;

				if (pm.isInParty(pUUID)) {
					Party party = pm.getParty(pUUID);
					if (party == null || party.getMembers().size() <= 1) {
						pt.handleSoloLogic(player, pUUID, processedPlayers);
						continue;
					}

					UUID leaderUUID = party.getLeader().getUniqueId();
					for (User member : party.getMembers()) processedPlayers.add(member.getUniqueId());

					Portal currentPortal = pt.getPortal(player.getLocation());
					if (currentPortal == null) {
						if (pt.removeCountdown(leaderUUID) != null)
							pt.sendPartyMessage(party, ColorUtils.chat(Alert.RED + " 이동이 취소되었습니다."));
						continue;
					}

					boolean allOnSamePortal = true;
					List<Player> onlinePartyPlayers = new ArrayList<>();

					for (User member : party.getMembers()) {
						Player mPlayer = Bukkit.getPlayer(member.getUniqueId());
						if (mPlayer == null || !currentPortal.isStepping(mPlayer)) {
							allOnSamePortal = false;
							break;
						}
						onlinePartyPlayers.add(mPlayer);
					}

					if (!allOnSamePortal) {
						if (pt.removeCountdown(leaderUUID) != null) {
							var msg = ColorUtils.chat(Alert.RED + " 이동이 취소되었습니다. 모든 파티원이 포탈위에 있어야합니다.");
							pt.sendPartyMessage(party, msg);
						}
						continue;
					}

					int secondsLeft = pt.getCountdown(leaderUUID, 3);


					if (secondsLeft <= 0) {
						pt.removeCountdown(leaderUUID);
						pt.sendPartyMessage(party, ColorUtils.chat(Alert.XmasLegacy + " 모든 파티원이 준비되었습니다. 서버를 이동합니다.."));

						for (Player mPlayer : onlinePartyPlayers)
							if (ServerTransfer.transfer(currentPortal.getDestination(), mPlayer, true, false)) {
								InfoUtils.warn(mPlayer, "이동 작업 시작중..");
								log.info("Server transfer initiated via Portal [{}] for player [{}] (Destination: {})",
										currentPortal.key(), player.getName(), currentPortal.getDestination());
							} else {
								InfoUtils.error(mPlayer, "서버 이동중 문제가 발생했습니다. 관리자를 호출해주세요.");
								log.error("Error occurred while transferring player using Portal Manager.");
							}
					} else {
						pt.sendPartyMessage(party, ColorUtils.chat(Alert.GREEN + " 모든 파티원이 입장했습니다. &6" + secondsLeft + "&f초 후 이동합니다."));
						pt.setCountdown(leaderUUID, secondsLeft - 1);
					}

				} else pt.handleSoloLogic(player, pUUID, processedPlayers);
			}
		}, 0L, 20L);
	}

	@Override
	public void stopTask() {
		if (this.task == null) return;
		this.task.cancel();
		this.task = null;
	}
}
