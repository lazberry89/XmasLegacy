package org.lazberry.xmaslegacy.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class CollectionHandler {

    private CollectionHandler() {} // 유틸 클래스 인스턴스화 방지

    /**
     * 원본 컬렉션의 변경에 영향받지 않으며 null-safe한 읽기 전용 스냅샷을 만듭니다.
     */
    public static <T> Collection<T> snapshot(@Nullable Collection<T> collection) {
        if (collection == null || collection.isEmpty()) return Collections.emptyList();

        List<T> list = new ArrayList<>(collection.size());
        for (T item : collection) {
            if (item != null) list.add(item);
        }
        return Collections.unmodifiableList(list);
    }

    public static <V> void editValues(@Nullable Collection<? extends V> collection,
                                      @NotNull Consumer<? super V> edit) {
        if (collection == null || collection.isEmpty()) return;

        for (V value : collection) {
            if (value != null) edit.accept(value);
        }
    }

    public static <V> void editValues(@Nullable Map<?, ? extends V> map,
                                      @NotNull Consumer<? super V> edit) {
        if (map == null || map.isEmpty()) return;

        for (V value : map.values()) {
            if (value != null) edit.accept(value);
        }
    }

    public static <V, R extends Collection<V>> R filter(@Nullable Collection<? extends V> collection,
                                                        @NotNull Predicate<? super V> predicate,
                                                        @NotNull Supplier<R> factory) {
        if (collection == null || collection.isEmpty()) return factory.get();

        return collection.stream()
                .filter(Objects::nonNull)
                .filter(predicate)
                .collect(Collectors.toCollection(factory));
    }

    public static <V> @Nullable V findFirst(@Nullable Collection<? extends V> collection,
                                            @NotNull Predicate<? super V> predicate) {
        if (collection == null || collection.isEmpty()) return null;
        for (V item : collection) {
            if (item != null && predicate.test(item)) {
                return item;
            }
        }
        return null;
    }

    public static <V> V findFirst(@Nullable Collection<? extends V> collection,
                                  @NotNull Predicate<? super V> predicate, V def) {
        V result = findFirst(collection, predicate);
        return result != null ? result : def;
    }

    public static <V> @Nullable V getRandom(@Nullable Collection<? extends V> collection) {
        if (collection == null || collection.isEmpty()) return null;

        int index = ThreadLocalRandom.current().nextInt(collection.size());
        if (collection instanceof List<? extends V> list) {
            return list.get(index);
        }

        int i = 0;
        for (V item : collection) {
            if (i++ == index) return item;
        }
        return null;
    }

    public static <V> Partition<V> partition(@Nullable Collection<? extends V> collection,
                                             @NotNull Predicate<? super V> predicate) {
        if (collection == null || collection.isEmpty()) {
            return Partition.toPartition(Collections.emptyList(), Collections.emptyList());
        }

        List<V> matches = new ArrayList<>();
        List<V> mismatches = new ArrayList<>();

        for (V item : collection) {
            if (item != null) {
                if (predicate.test(item)) matches.add(item);
                else mismatches.add(item);
            }
        }
        return Partition.toPartition(matches, mismatches);
    }
}