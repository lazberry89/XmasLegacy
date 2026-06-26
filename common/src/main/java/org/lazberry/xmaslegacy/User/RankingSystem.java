package org.lazberry.xmaslegacy.User;

import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public enum RankingSystem {
    INSTANCE;

    private final @NotNull UserManager um;

    RankingSystem() {
        this.um = UserManager.INSTANCE;
    }

    /**
     * @param type what kind of info to get
     * @return async returned. Use chaining to get Async-fy value
     */
    @NonBlocking
    public @NotNull CompletableFuture<@Nullable User> getFirst(@NotNull RankType type) {
        return getRank(type, 1).thenApply(u -> u.isEmpty() ? null : u.getFirst());
    }

    /**
     * @param type what kind of info to get
     * @return async returned. Use chaining to get Async-fy value
     */
    @NonBlocking
    public @NotNull CompletableFuture<@Nullable User> getLast(@NotNull RankType type) {
        return getRank(type, 1).thenApply(u -> u.isEmpty() ? null : u.getLast());
    }

    /**
     * @param type about what kind of Rank info to get
     * @param limit how much info you want to get
     * @return async returned. User CompletableFuture for chaining
     */
    @NonBlocking
    public @NotNull CompletableFuture<List<User>> getRank(@NotNull RankType type, int limit) {
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
