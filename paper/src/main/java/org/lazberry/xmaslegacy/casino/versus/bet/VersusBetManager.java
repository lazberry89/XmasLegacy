package org.lazberry.xmaslegacy.casino.versus.bet;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.bags.BagManager;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.casino.versus.GameResult;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

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

	public void coinApplyProcessByResult(GameResult result, UUID winner) {
		if (betRepository.isEmpty()) return;
		betRepository.forEach((uuid, record) -> {
			int amount = record.amount();
			if (amount <= 0) return;
			var select = record.selected();

			boolean win = Objects.equals(select, winner);
			switch (result) {
				case RED, BLUE -> {
					int giveAmount;
					if (win) {
						giveAmount = amount * 2;
					} else {
						giveAmount = amount / 2;
					}
					bm.addItem(uuid, Casino.coin(giveAmount));
				}
			}
		});
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
