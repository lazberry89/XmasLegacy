package xmaslegacy.TransferPortal;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Party.Party;
import org.lazberry.xmaslegacy.Party.PartyManager;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Utils.ServerTransfer;
import xmaslegacy.PluginUtils.ServerType;
import xmaslegacy.XmasLegacy;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public enum PortalManager {
	INSTANCE;

    private final @NotNull Map<String, Portal> portalMap = new HashMap<>();
    private final @NotNull Set<Portal> portalSet = new HashSet<>();
    private final @NotNull XmasLegacy plugin;
    private final @NotNull Map<UUID, Integer> activeCountdowns = new ConcurrentHashMap<>();

    PortalManager() {
        this.plugin = XmasLegacy.getInstance();
    }

    public void addPortal(@NotNull String key, @NotNull Location loc, @NotNull ServerType destination) {
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

    private void sendPartyMessage(@NotNull Party party, @NotNull Component message) {
        party.getMembers().stream()
                .map(u -> Bukkit.getPlayer(u.getUUID()))
                .filter(Objects::nonNull)
                .filter(Entity::isValid)
                .filter(Player::isOnline)
                .forEach(p -> p.sendMessage(message));
    }

    public void startPortalScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            @NotNull Set<UUID> processedPlayers = new HashSet<>();
            var pm = PartyManager.INSTANCE;

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID pUUID = player.getUniqueId();
                if (processedPlayers.contains(pUUID)) continue;

                if (pm.isInParty(pUUID)) {
                    Party party = pm.getParty(pUUID);
                    if (party == null || party.getMembers().size() <= 1) {
                        handleSoloLogic(player, pUUID, processedPlayers);
                        continue;
                    }

                    UUID leaderUUID = party.getLeader().getUUID();
                    for (User member : party.getMembers()) processedPlayers.add(member.getUUID());

                    Portal currentPortal = getPortal(player.getLocation());
                    if (currentPortal == null) {
                        if (activeCountdowns.remove(leaderUUID) != null)
                            sendPartyMessage(party, ColorUtils.chat(Alert.RED + " 이동이 취소되었습니다."));
                        continue;
                    }

                    boolean allOnSamePortal = true;
                    List<Player> onlinePartyPlayers = new ArrayList<>();

                    for (User member : party.getMembers()) {
                        Player mPlayer = Bukkit.getPlayer(member.getUUID());
                        if (mPlayer == null || !currentPortal.isStepping(mPlayer)) {
                            allOnSamePortal = false;
                            break;
                        }
                        onlinePartyPlayers.add(mPlayer);
                    }

                    if (!allOnSamePortal) {
                        if (activeCountdowns.remove(leaderUUID) != null) {
                            var msg = ColorUtils.chat(Alert.RED + " 이동이 취소되었습니다. 모든 파티원이 포탈위에 있어야합니다.");
                            sendPartyMessage(party, msg);
                        }
                        continue;
                    }

                    int secondsLeft = activeCountdowns.getOrDefault(leaderUUID, 3);

                    if (secondsLeft <= 0) {
                        activeCountdowns.remove(leaderUUID);
                        sendPartyMessage(party, ColorUtils.chat(Alert.XmasLegacy + " 모든 파티원이 준비되었습니다. 서버를 이동합니다.."));

                        for (Player mPlayer : onlinePartyPlayers)
                            ServerTransfer.transfer(currentPortal.getDestination(), mPlayer, true, false);
                    } else {
                        sendPartyMessage(party, ColorUtils.chat(Alert.GREEN + " 모든 파티원이 입장했습니다. &6" + secondsLeft + "&f초 후 이동합니다."));
                        activeCountdowns.put(leaderUUID, secondsLeft - 1);
                    }

                } else handleSoloLogic(player, pUUID, processedPlayers);
            }
        }, 0L, 20L);
    }

    /**
     * 중복 방지 및 솔로 포탈 이동 로직 공통 분리
     */
    private void handleSoloLogic(@NotNull Player player, @NotNull UUID pUUID, @NotNull Set<UUID> processedPlayers) {
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
}
