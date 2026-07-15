package org.lazberry.xmaslegacy.TransferPortal;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.Party;
import org.lazberry.xmaslegacy.PluginUtils.Initializers;
import org.lazberry.xmaslegacy.Utils.ServerTransfer;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Registry
public enum PortalManager implements ServerManager {
	INSTANCE;

    private final @NotNull Map<String, Portal> portalMap = new HashMap<>();
    private final @NotNull Set<Portal> portalSet = new HashSet<>();
    private final @NotNull Map<UUID, Integer> activeCountdowns = new ConcurrentHashMap<>();

	@Nullable Integer removeCountdown(@NotNull UUID uuid) {
		return this.activeCountdowns.remove(uuid);
	}
	void setCountdown(@NotNull UUID uuid, int countdown) {
		this.activeCountdowns.put(uuid, countdown);
	}
	int getCountdown(@NotNull UUID uuid, int def) {
		return this.activeCountdowns.getOrDefault(uuid, def);
	}

    PortalManager() {}

    public void addPortal(@NotNull String key, @NotNull Location loc, @NotNull Initializers destination) {
        Portal portal = new Portal(key, loc, destination);
        this.portalMap.put(key, portal);
        this.portalSet.add(portal);
    }

    public boolean removePortal(@NotNull String key) {
        Portal portal = this.portalMap.remove(key);
        if (portal == null) return false;

        this.portalSet.remove(portal);
        return true;
    }

    public @Nullable Portal getPortal(@NotNull Location loc) {
        return this.portalSet.stream()
                .filter(p -> p.isStepping(loc))
                .findFirst().orElse(null);
    }

    public @Nullable Portal getPortal(@NotNull String key) {
        return this.portalMap.get(key);
    }
    public @Nullable Portal getPortal(@NotNull Player player) {
        return getPortal(player.getLocation());
    }

    void sendPartyMessage(@NotNull Party party, @NotNull Component message) {
        party.getMembers().stream()
                .map(u -> Bukkit.getPlayer(u.getUniqueId()))
                .filter(Objects::nonNull)
                .filter(Entity::isValid)
                .filter(Player::isOnline)
                .forEach(p -> p.sendMessage(message));
    }

    /**
     * 중복 방지 및 솔로 포탈 이동 로직 공통 분리
     */
     void handleSoloLogic(@NotNull Player player, @NotNull UUID pUUID, @NotNull Set<UUID> processedPlayers) {
        processedPlayers.add(pUUID);
        Portal portal = getPortal(player.getLocation());

        if (portal == null) {
            activeCountdowns.remove(pUUID);
            return;
        }

        int secondsLeft = activeCountdowns.getOrDefault(pUUID, 3);

        if (secondsLeft <= 0) {
            activeCountdowns.remove(pUUID);
            player.sendMessage(ColorUtils.chat(Alert.XmasLegacy + " 서버를 이동합니다."));
            ServerTransfer.transfer(portal.getDestination(), player, true, false);
        } else {
            player.sendMessage(ColorUtils.chat(String.format("%s 서버 이동까지 &6%d&f초 남음. 그대로 자리에 머무르세요.", Alert.YELLOW, secondsLeft)));
            activeCountdowns.put(pUUID, secondsLeft - 1);
        }
    }

	@Override
	public void init() {

	}
}
