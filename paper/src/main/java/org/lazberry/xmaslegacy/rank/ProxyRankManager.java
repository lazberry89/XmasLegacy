package org.lazberry.xmaslegacy.rank;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
@Registry.Exclude(type = ServerType.LOBBY)
public class ProxyRankManager implements Initiator {
	private final Map<RankType, ArrayList<RankManager.RankData>> cachedRanks = new ConcurrentHashMap<>();
	private final UserManager userManager;
	private final RankManager rankManager;

	@Getter @Setter
	private volatile int maxUpdateSize = 100;

	@Inject
    public ProxyRankManager(UserManager userManager, RankManager rankManager) {
        this.userManager = userManager;
        this.rankManager = rankManager;
    }

    @Override
	public void init() {
		cachedRanks.clear();
		updateAll();
	}

	public ArrayList<RankManager.RankData> getRankList(RankType rankType) {
		return cachedRanks.get(rankType);
	}

	public int getRank(RankType rankType, UUID uuid) {
		var list = cachedRanks.get(rankType);
		if (list == null) return -1;

		return list.stream()
				.filter(r -> r.uuid().equals(uuid))
				.findFirst()
				.map(RankManager.RankData::rank)
				.orElse(-1);
	}

	public void updateRanks() {
		Arrays.stream(RankType.values()).forEach(type ->
			rankManager.getRankByLimit(type, maxUpdateSize).whenComplete((list, ex) -> {
				if (ex != null) {
					log.error("Failed to update rank cache.", ex);
					return;
				}
				cachedRanks.put(type, list);
			}));
	}

	private void updateAll() {
		forEachOnlineUsers(rankManager::updateUser);
	}

	private void forEachOnlineUsers(Consumer<User> users) {
		Bukkit.getOnlinePlayers().stream()
				.filter(Player::isValid)
				.map(Player::getUniqueId)
				.map(userManager::getUser)
				.filter(Objects::nonNull)
				.forEach(users);
	}

	@Override
	public void close() {
		cachedRanks.clear();
		updateAll();
	}
}
