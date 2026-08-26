package org.lazberry.xmaslegacy.collectors.game;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmaslegacy.collectors.field.Field;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.OptionalUtils;
import org.lazberry.xmaslegacy.utils.ServerTransfer;

import java.util.*;
import java.util.stream.Stream;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Session {
	@EqualsAndHashCode.Include
	private final CollectorsManager cm;
	private final Difficulty difficulty;
	private final Field field;
	private final Set<User> playingUsers = new HashSet<>();
	private final Map<User, Location> originLocation = new HashMap<>();

	private BukkitTask loopTask;
	private final int MAX_PLAYERS = 10;
	private final int MAX_POTS;
	private int tickCounter = 0;

	public Session(Field field, CollectorsManager cm) {
		this.cm = cm;
		this.field = field;
		this.difficulty = field.getDifficulty();
		this.MAX_POTS = difficulty.getDropCount();
	}

	private Stream<Player> playerStream() {
		return playingUsers.stream()
				.map(User::getUniqueId)
				.map(Bukkit::getPlayer)
				.filter(Objects::nonNull)
				.filter(Player::isValid)
				.filter(Player::isOnline);
	}

	public boolean addUser(User user) {
		if (playingUsers.size() >= MAX_PLAYERS) return false;

		boolean entry = playingUsers.add(user);
		if (!entry) return false;

		OptionalUtils.ifNotNullOrElse(Bukkit.getPlayer(user.getUniqueId()), p -> {
					originLocation.put(user, p.getLocation());
					cm.addBackup(p, p.getInventory().getContents());
					p.getInventory().clear();
					ServerTransfer.dramaticTeleport(p, field.getSpawn());
				}, () -> {});
		return true;
	}

	public boolean removeUser(User user) {
		if (!playingUsers.remove(user)) return false;

		OptionalUtils.ifNotNullOrElse(Bukkit.getPlayer(user.getUniqueId()), p -> {
			cm.applyBackup(p);
			p.setWalkSpeed(0.2f);
			p.sendActionBar(ColorUtils.chat(" "));
			Location origin = originLocation.get(user);

			if (origin == null) {
				InfoUtils.error(p, "돌아갈 위치가 설정되지 않았습니다. 서버 스폰으로 이동합니다.");
				p.teleport(field.getWorld().getSpawnLocation());
				return;
			}
			p.teleport(origin);
		}, () -> {});
		originLocation.remove(user);
		return true;
	}

	public boolean isSessionUser(User user) {
		return playingUsers.contains(user);
	}

	public void startLoop() {
		if (loopTask != null || field.isRunning()) return;
		field.setRunning(true);

		loopTask = Bukkit.getScheduler().runTaskTimer(cm.getPlugin(), () -> {
			tickCounter++;

			playerStream().forEach(p -> {
				cm.applyWeightSlowness(p);
				p.sendActionBar(ColorUtils.chat("&6무게&f " + cm.getWeight(p) + " &7/ 200"));
			});

			if (tickCounter >= 5) {
				field.replenishDropContainers(MAX_POTS);
				tickCounter = 0;
			}
		}, 0L, 20L);
	}

	public void stopLoop() {
		if (loopTask != null) {
			loopTask.cancel();
			loopTask = null;
		}
		field.setRunning(false);
		field.cleanDropContainers();
		new HashSet<>(playingUsers).forEach(this::removeUser);
	}
}