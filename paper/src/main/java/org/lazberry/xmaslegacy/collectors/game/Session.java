package org.lazberry.xmaslegacy.collectors.game;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.lazberry.xmaslegacy.collectors.field.Field;
import org.lazberry.xmaslegacy.user.User;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.OptionalUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Session {
	@EqualsAndHashCode.Include
	private final CollectorsManager cm;
	private final Difficulty difficulty;
	private final Field field;
	private final Set<User> playingUsers = new HashSet<>();

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

		OptionalUtils.ifNotNullOrElse(Bukkit.getPlayer(user.getUniqueId()),
				p -> {
					cm.addBackup(p, p.getInventory().getContents());
					p.getInventory().clear();
					p.teleport(field.getSpawn());
					// TODO: 던전 스폰 위치로 텔레포트 필요 (예: p.teleport(field.getSpawn());)
				}, () -> {});
		return playingUsers.add(user);
	}

	public boolean removeUser(User user) {
		OptionalUtils.ifNotNullOrElse(Bukkit.getPlayer(user.getUniqueId()),
				p -> {
					cm.applyBackup(p);
					p.setWalkSpeed(0.2f);
					p.sendActionBar(ColorUtils.chat(" "));
					// TODO: 로비로 텔레포트 필요
				}, () -> {});
		return playingUsers.remove(user);
	}

	public boolean isSessionUser(User user) {
		return playingUsers.contains(user);
	}

	// 서버가 켜질 때 실행되는 무한 던전 가동기
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