package org.lazberry.xmaslegacy.casino.versus.bet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.casino.versus.GameResult;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Components.Include(type = ServerType.MAIN)
public class VersusBetManager {
    private final Map<UUID, BetRecord> betRepository = new ConcurrentHashMap<>();

    public void bet(Player p, UUID select, int amount) {
        UUID uuid = p.getUniqueId();
        betRepository.put(uuid, new BetRecord(select, amount));
    }

	public Collection<Player> getValidBetPlayers() {
		return betRepository.keySet().stream()
				.map(Bukkit::getPlayer)
				.filter(Objects::nonNull)
				.filter(Player::isValid)
				.collect(Collectors.toSet());
	}

	public void giveBack() {
		betRepository.forEach((uuid, record) -> payout(uuid, record.amount()));
	}

	public void coinApplyProcessByResult(GameResult result, @Nullable UUID winner) {
		if (betRepository.isEmpty()) return;

		// No winner was decided: everyone gets their stake back.
		if (result == GameResult.ERROR || result == GameResult.LEAVE) {
			giveBack();
			clearBets();
			return;
		}

		betRepository.forEach((uuid, record) -> {
			int amount = record.amount();
			if (amount <= 0) return;
			var select = record.selected();

			var player = Bukkit.getPlayer(uuid);
			boolean isOnline = player != null && player.isOnline();

			switch (result) {
				case RED, BLUE -> {
					boolean win = Objects.equals(select, winner);
					if (win) {
						int giveAmount = amount * 2;
						payout(uuid, giveAmount);
						if (isOnline) Casino.sendIconAlert(player, "베팅한 플레이어가 승리하였습니다! 지급된 코인을 확인하세요.");
					} else {
						if (isOnline) InfoUtils.error(player, "베팅한 플레이어가 패배하였습니다.");
					}
				}
				case DRAW -> {
					int giveAmount = amount / 2;
					if (giveAmount > 0) payout(uuid, giveAmount);
					if (isOnline) InfoUtils.warn(player, "경기가 무승부로 끝나 베팅 코인의 절반이 반환되었습니다.");
				}
			}
		});
		clearBets();
	}

	/** Offline bettors get nothing: there is no reliable place to keep coins for them yet. */
	private void payout(UUID uuid, int amount) {
		if (amount > 0) Casino.giveCoin(Bukkit.getPlayer(uuid), amount);
	}

    public void remove(UUID uuid) {
        betRepository.remove(uuid);
    }

    public Optional<BetRecord> getBetRecord(UUID uuid) {
        return Optional.ofNullable(betRepository.get(uuid));
    }

    public void clearBets() {
        betRepository.clear();
    }

    public record BetRecord(UUID selected, int amount) {}
}
