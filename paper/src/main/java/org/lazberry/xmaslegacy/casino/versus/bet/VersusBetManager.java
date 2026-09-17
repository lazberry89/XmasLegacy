package org.lazberry.xmaslegacy.casino.versus.bet;

import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Registry.Include(type = ServerType.MAIN)
public class VersusBetManager {
    private final Map<UUID, BetRecord> betRepository = new ConcurrentHashMap<>();

    public VersusBetManager() {}

    public void bet(Player p, UUID select, int amount) {
        UUID uuid = p.getUniqueId();
        betRepository.put(uuid, new BetRecord(select, amount));
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
