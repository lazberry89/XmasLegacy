package org.lazberry.xmaslegacy.collectors.game;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.collectors.backup.CollectorsConfig;
import org.lazberry.xmaslegacy.collectors.drop.DropsManager;
import org.lazberry.xmaslegacy.collectors.field.FieldManager;
import org.lazberry.xmaslegacy.party.PartyManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.user.UserManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.InventorySerializer;
import org.lazberry.xmaslegacy.utils.OptionalUtils;
import org.lazberry.xmaslegacy.utils.UserHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Registry.Include(type = ServerType.MAIN)
public class CollectorsManager implements Initiator {
	private final Map<UUID, ItemStack[]> backup = new ConcurrentHashMap<>();
	private final Map<Difficulty, Session> sessionByDifficulty = new HashMap<>(5);
	private final Map<User, Session> sessionMap = new HashMap<>();
	private final CollectorsConfig config;
	private final XmasLegacy plugin;
	private final TicketRepository tr;
	private final DropsManager dm;
	private final UserManager um;
	private final PartyManager pm;
	private final FieldManager fm;

	@Inject
	public CollectorsManager(XmasLegacy plugin,
	                         CollectorsConfig config,
	                         TicketRepository tr,
	                         DropsManager dm,
	                         UserManager um,
	                         PartyManager pm,
							 FieldManager fm) {
		this.config = config;
		this.plugin = plugin;
		this.tr = tr;
		this.dm = dm;
		this.um = um;
		this.pm = pm;
		this.fm = fm;
	}

	@Override
	public void init() {
		loadBackup();
	}

	@Override
	public void close() {
		Map<UUID, String> serializedBackup = backup.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> InventorySerializer.serializeContents(entry.getValue())
				));
		config.saveSync(serializedBackup);
	}

	public void join(Player player) {
		ItemStack item = player.getInventory().getItemInMainHand();
		UUID uuid = player.getUniqueId();
		if (!tr.isTicket(item)) {
			InfoUtils.error(player, "&6참가 티켓&f을 들고있어야합니다!");
			return;
		}
		Difficulty difficulty = tr.getDifficultyByTicket(item);
		if (difficulty == null) {
			InfoUtils.error(player, "티켓이 &c손상&f되었습니다. 관리자에게 문의해주세요.");
			return;
		}
		Session session = getOrCreateSession(difficulty);
		if (session == null) {
			InfoUtils.error(player, "아직 필드가 설정되지 않았습니다. 기다려주세요!");
			return;
		}
		OptionalUtils.ifNotNullOrElse(um.getUser(uuid), u -> {
			if (sessionMap.containsKey(u)) {
				InfoUtils.error(player, "이미 세션에 참가중입니다. 퇴장 후 다시 입장해주세요!");
				return;
			}
			if (session.addUser(u)) {
				InfoUtils.info(player, "게임에 참가했습니다.");
				item.setAmount(0);
				sessionMap.put(u, session);
			}
			else InfoUtils.error(player, "유저가 너무 많습니다! 조금만 기다려주세요. &c{}/{}",
					session.getPlayingUsers().size(), session.getMAX_PLAYERS());
		}, () -> {
			InfoUtils.error(player, "참가에 실패했습니다.");
			UserHandler.loadUser(player, true);
		});
	}

	public void leave(Player player) {
		UUID uuid = player.getUniqueId();
		OptionalUtils.ifNotNullOrElse(um.getUser(uuid), u -> {
			Session session = getSession(u);
			if (session == null || !session.removeUser(u)) {
				InfoUtils.error(player, "현재 참여중인 게임이 없습니다!");
				return;
			}
			sessionMap.remove(u);
			InfoUtils.info(player, "게임에서 퇴장했습니다.");
		}, () -> {
			InfoUtils.error(player, "퇴장 중 오류가 발생했습니다.");
			UserHandler.loadUser(player, true);
		});
	}

	public @Nullable Session getOrCreateSession(Difficulty difficulty) {
		var field = fm.getField(difficulty);

        return field.map(value -> sessionByDifficulty.computeIfAbsent(difficulty,
                diff -> new Session(value, this))).orElse(null);
    }

	public @Nullable Session getSession(User user) {
		return sessionMap.get(user);
	}

	public int getWeight(Player player) {
		return dm.weightOfInventory(player);
	}

	public int getPrice(Player player) {
		return dm.calculatePriceOfInventory(player);
	}

	public void applyWeightSlowness(Player player) {
		int weight = dm.weightOfInventory(player);
		float defaultSpeed = 0.2f;

		if (weight >= 200) {
			player.setWalkSpeed(0.015f);
			return;
		}

		if (weight <= 0) {
			player.setWalkSpeed(defaultSpeed);
			return;
		}

		double weightRatio = (double) weight / 200.0;

		float minSpeed = 0.02f;
		float calculatedSpeed = (float) (defaultSpeed - (weightRatio * (defaultSpeed - minSpeed)));

		player.setWalkSpeed(calculatedSpeed);
	}

	public void loadBackup() {
		config.load().whenComplete((r, e) -> {
			if (e != null) {
				log.error("Exception occurred while loading backup.");
				return;
			}
			if (!r.isEmpty()) backup.putAll(r);
		});
	}

	public void addBackup(Player player, ItemStack[] contents) {
		ItemStack[] clonedContents = Arrays.stream(contents)
				.map(item -> item != null ? item.clone() : null)
				.toArray(ItemStack[]::new);

		backup.put(player.getUniqueId(), clonedContents);
		config.save(player);
	}

	public void applyBackup(Player player) {
		var uuid = player.getUniqueId();
		if (hasBackup(uuid)) {
			player.getInventory().setContents(backup.get(uuid));
			backup.remove(uuid);
			config.removeBackup(uuid).exceptionally(e -> {
				log.error("Error occurred while applying backup to player {}.", player.getName());
				return null;
			});
		}
	}

	public boolean hasBackup(UUID uuid) {
		return backup.containsKey(uuid);
	}
}


