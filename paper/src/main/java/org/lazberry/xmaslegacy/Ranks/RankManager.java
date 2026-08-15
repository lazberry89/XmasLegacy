package org.lazberry.xmaslegacy.Ranks;


import lombok.Getter;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.User.RankType;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.Framework.Initiator;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Registry.Exclude(type = ServerType.LOBBY)
public class RankManager implements Initiator {
	private final @NotNull @Getter UserManager userManager;
	private @NotNull volatile List<User> dollarRank = List.of();
	private @NotNull volatile List<User> expRank = List.of();
	private @NotNull volatile List<User> roleExpRank = List.of();
	private @NotNull volatile List<User> playtimeRank = List.of();

	@Inject
	public RankManager(@NotNull UserManager userManager) {
		this.userManager = userManager;
	}

	@Override
	public void init() {}

	/**
	 * Only used at Async task replacing values. Do not use as general {@link lombok.Setter}
	 * @param users unmodifiable List of users.
	 * @see User
	 * @see List
	 * @see org.jetbrains.annotations.Unmodifiable
	 */
	void replaceDollarRank(@NotNull List<User> users) {
		dollarRank = users;
	}
	void replaceExpRank(@NotNull List<User> users) {
		expRank = users;
	}
	void replaceRoleExpRank(@NotNull List<User> users) {
		roleExpRank = users;
	}
	void replacePlaytime(@NotNull List<User> users) {
		playtimeRank = users;
	}

	/**
	 * @param type about what kind of Rank info to get
	 * @param limit how much info you want to get
	 * @return async returned. User CompletableFuture for chaining
	 */
	@NonBlocking
	@NotNull CompletableFuture<List<User>> getRank(@NotNull RankType type, int limit) {
		var users = userManager.getUsers();
		if (users.isEmpty()) return CompletableFuture.completedFuture(List.of());
		return switch (type) {
			case DOLLAR -> CompletableFuture.supplyAsync(() ->
					userManager.getUsers().stream()
							.sorted(Comparator.comparingInt(User::getDollars).reversed())
							.limit(limit)
							.toList());
			case EXP -> CompletableFuture.supplyAsync(() ->
					userManager.getUsers().stream()
							.sorted(Comparator.comparingDouble(User::getExp).reversed())
							.limit(limit)
							.toList());
			case ROLE_EXP -> CompletableFuture.supplyAsync(() ->
					userManager.getUsers().stream()
							.sorted(Comparator.comparingDouble(User::getRoleExp).reversed())
							.limit(limit)
							.toList());
			case PLAYTIME -> CompletableFuture.supplyAsync(() ->
					userManager.getUsers().stream()
							.sorted(Comparator.comparingInt(User::getPlayTime).reversed())
							.limit(limit)
							.toList());
		};
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
}
