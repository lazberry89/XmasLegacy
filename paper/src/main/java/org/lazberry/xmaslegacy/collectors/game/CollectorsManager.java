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
		OptionalUtils.ifNotNullOrElse(um.getUser(player.getUniqueId()),
				u -> {
					var ticket = player.getInventory().getItemInMainHand();
					Difficulty difficulty = tr.getDifficultyByTicket(ticket);
					if (difficulty == null) {
						InfoUtils.error(player, "티켓이 아닙니다!");
						return;
					}
				}, () -> UserHandler.loadUser(player, true));
	}

	public Session getOrCreateSession(Difficulty difficulty) {
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


