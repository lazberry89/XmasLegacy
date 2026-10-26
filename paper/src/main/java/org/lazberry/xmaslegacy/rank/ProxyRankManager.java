package org.lazberry.xmaslegacy.rank;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.framework.Initiator;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.ColorUtils;

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

	public Component getUserRankOverview(UUID uuid) {
		User user = userManager.getProxyUser(uuid);
		if (user == null) {
			return ColorUtils.chat(Alert.RED + "유저 정보를 조회할 수 없습니다.");
		}

		int dollarRank = getRank(RankType.DOLLAR, uuid);
		int expRank = getRank(RankType.EXP, uuid);
		int roleExpRank = getRank(RankType.ROLE_EXP, uuid);
		int playtimeRank = getRank(RankType.PLAYTIME, uuid);

		String name = Bukkit.getOfflinePlayer(uuid).getName();
		if (name == null) name = "Unknown";

		String formattedMessage = String.format(
				"""
						<yellow><b>[ %s 님의 랭킹 정보 ]</b></yellow>
						<dark_gray>--------------------------------</dark_gray>
						💰 <gold>자산 랭킹:</gold> %s
						✨ <green>경험치 랭킹:</green> %s
						⚔ <red>직업 경험치 랭킹:</red> %s
						⏳ <aqua>플레이 시간 랭킹:</aqua> %s
						<dark_gray>--------------------------------</dark_gray>""",
				name,
				formatRankString(dollarRank),
				formatRankString(expRank),
				formatRankString(roleExpRank),
				formatRankString(playtimeRank)
		);

		return ColorUtils.minimessage(formattedMessage);
	}

	private String formatRankString(int rank) {
		if (rank <= 0) {
			return "<gray>순위 권외 (-위)</gray>";
		}
		return String.format("<yellow><b>%d</b>위</yellow>", rank);
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

	public void updateAll() {
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
