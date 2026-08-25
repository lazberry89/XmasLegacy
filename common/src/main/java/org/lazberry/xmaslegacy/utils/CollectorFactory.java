package org.lazberry.xmaslegacy.utils;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CollectorFactory {

    @Contract("-> fail")
    @ApiStatus.Internal
    private CollectorFactory() {
        throw new UnsupportedOperationException("Utility Class");
    }

    public static <T> Collector<T, ?, Partition<T>> toPartition(Predicate<? super T> predicate) {
        return Collectors.collectingAndThen(
                Collectors.partitioningBy(predicate),
                map -> Partition.toPartition(map.get(true), map.get(false))
        );
    }

    public static <T, D extends Collection<T>> Collector<T, ?, Partition<T>> toPartition(
            Predicate<? super T> predicate,
            Collector<T, ?, D> downstream
    ) {
        return Collectors.collectingAndThen(
                Collectors.partitioningBy(predicate, downstream),
                map -> Partition.toPartition(map.get(true), map.get(false))
        );
    }

    public static <T, R> Collector<T, ?, Partition<R>> mappingToPartition(
            Function<? super T, ? extends R> mapper,
            Predicate<? super R> predicate
    ) {
        return Collectors.mapping(mapper, toPartition(predicate));
    }
}
