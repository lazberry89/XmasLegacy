package org.lazberry.xmaslegacy.casino.versus.bet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.bags.BagManager;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.casino.versus.GameResult;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Registry.Include(type = ServerType.MAIN)
public class VersusBetManager {
    private final Map<UUID, BetRecord> betRepository = new ConcurrentHashMap<>();
	private final BagManager bm;

	@Inject
    public VersusBetManager(BagManager bm) {
	    this.bm = bm;
    }

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
		if (betRepository.isEmpty()) return;
		betRepository.forEach((uuid, record) -> {
			int amount = record.amount();
			if (amount <= 0) return;

			var player = Bukkit.getPlayer(uuid);
			if (player != null && player.isOnline()) {
				var remains = player.getInventory().addItem(Casino.coin(amount));
				if (!remains.isEmpty()) bm.addAll(player, remains.values());
			} else {
				bm.addItem(uuid, Casino.coin(amount));
			}
		});
	}

	public void coinApplyProcessByResult(GameResult result, @Nullable UUID winner) {
		if (betRepository.isEmpty()) return;

		if (result == GameResult.ERROR) {
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
						payout(uuid, player, giveAmount);
						if (isOnline) Casino.sendIconAlert(player, "베팅한 플레이어가 승리하였습니다! 지급된 코인을 확인하세요.");
					} else {
						if (isOnline) InfoUtils.error(player, "베팅한 플레이어가 패배하였습니다.");
					}
				}
				case DRAW -> {
					int giveAmount = amount / 2;
					if (giveAmount > 0) payout(uuid, player, giveAmount);
					if (isOnline) InfoUtils.warn(player, "경기가 무승부로 끝나 베팅 코인의 절반이 반환되었습니다.");
				}
			}
		});
		clearBets();
	}

	private void payout(UUID uuid, Player player, int amount) {
		if (player != null && player.isOnline()) {
			var remains = player.getInventory().addItem(Casino.coin(amount));
			if (!remains.isEmpty()) bm.addAll(player, remains.values());
		} else {
			bm.addItem(uuid, Casino.coin(amount));
		}
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
