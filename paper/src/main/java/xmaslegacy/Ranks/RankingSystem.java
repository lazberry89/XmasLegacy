package xmaslegacy.Ranks;

import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.User.RankType;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import xmaslegacy.XmasLegacy;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
public enum RankingSystem {
    INSTANCE;

	private final @NotNull XmasLegacy plugin;
    private final @NotNull UserManager um;
	private @NotNull volatile List<User> dollarRank = List.of();
	private @NotNull volatile List<User> expRank = List.of();
	private @NotNull volatile List<User> roleExpRank = List.of();
	private @NotNull volatile List<User> playtimeRank = List.of();
	private final @NotNull Map<RankType, BukkitTask> tasks = new HashMap<>();

    RankingSystem() {
		this.plugin = XmasLegacy.getInstance();
        this.um = UserManager.INSTANCE;
    }

	public @NotNull List<User> rank(@NotNull RankType type) {
		return switch (type) {
			case DOLLAR -> this.dollarRank;
			case EXP -> this.expRank;
			case ROLE_EXP -> this.roleExpRank;
			case PLAYTIME -> this.playtimeRank;
		};
	}

	/**
	 * @param type the wanted type of Rank
	 * @param user target user.
	 * @return if none return -1, returned value is rank.
	 */
	public int getRank(@NotNull RankType type, @NotNull User user) {
		List<User> cachedRank = this.rank(type);
		int index = cachedRank.indexOf(user);

		return index == -1 ? -1 : index + 1;
	}

    /**
     * @param type what kind of info to get
     * @return cache returned.
     */
    @NonBlocking
    public @Nullable User getFirst(@NotNull RankType type) {
        var cache = this.rank(type);
		return cache.isEmpty() ? null : cache.getFirst();
    }

	@NonBlocking
	public @Nullable User getLast(@NotNull RankType type) {
		var cache = this.rank(type);
		return cache.isEmpty() ? null : cache.getLast();
	}

	private @NotNull BukkitTask rankTask(@NotNull RankType type) {
		var task = tasks.get(type);
		if (task != null) return task;

		log.warn("Ranking task (TYPE : {}) started.", type);

		var newTask = Bukkit.getScheduler().runTaskTimer(plugin, () ->
			getRank(type, 100).whenComplete((lst, ex) -> {
				if (ex != null) {
					log.error("Task occurred error while starting rank task. (TYPE : {})", type);
					return;
				}
				switch (type) {
					case DOLLAR -> this.dollarRank = lst;
					case EXP -> this.expRank = lst;
					case ROLE_EXP -> this.roleExpRank = lst;
					case PLAYTIME -> this.playtimeRank = lst;
				}
			}), 0L, 20L);
		this.tasks.put(type, newTask);
		return newTask;
	}

	public void startRankTask(@NotNull RankType type) {
		var task = rankTask(type);
		this.tasks.put(type, task);
	}

	public void startRankTask() {
		Arrays.stream(RankType.values()).forEach(r -> {
			var task = this.rankTask(r);
			this.tasks.put(r, task);
		});
	}

	public void stopRankTask(@NotNull RankType type) {
		var task = tasks.get(type);
		if (task != null) task.cancel();
		tasks.remove(type);
	}

	public void stopRankTask() {
		this.tasks.keySet().forEach(this::stopRankTask);
		log.warn("Stopping all rank task.");
	}


    /**
     * @param type about what kind of Rank info to get
     * @param limit how much info you want to get
     * @return async returned. User CompletableFuture for chaining
     */
    @NonBlocking
    private @NotNull CompletableFuture<List<User>> getRank(@NotNull RankType type, int limit) {
        var users = um.getUsers();
        if (users.isEmpty()) return CompletableFuture.completedFuture(List.of());
        return switch (type) {
            case DOLLAR -> CompletableFuture.supplyAsync(() ->
                    um.getUsers().stream()
                            .sorted(Comparator.comparingInt(User::getDollars).reversed())
                            .limit(limit)
                            .toList());
            case EXP -> CompletableFuture.supplyAsync(() ->
                    um.getUsers().stream()
                            .sorted(Comparator.comparingDouble(User::getExp).reversed())
                            .limit(limit)
                            .toList());
            case ROLE_EXP -> CompletableFuture.supplyAsync(() ->
                    um.getUsers().stream()
                            .sorted(Comparator.comparingDouble(User::getRoleExp).reversed())
                            .limit(limit)
                            .toList());
            case PLAYTIME -> CompletableFuture.supplyAsync(() ->
                    um.getUsers().stream()
                            .sorted(Comparator.comparingInt(User::getPlayTime).reversed())
                            .limit(limit)
                            .toList());
        };
    }
}
