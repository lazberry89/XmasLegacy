package org.lazberry.xmaslegacy.utils;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class Partition<E> extends AbstractCollection<E> {
    private final List<E> matches;
    private final List<E> mismatches;

    @ApiStatus.Internal
    private Partition(Collection<? extends E> matches, Collection<? extends E> mismatches) {
        this.matches = matches != null ? new ArrayList<>(matches) : Collections.emptyList();
        this.mismatches = mismatches != null ? new ArrayList<>(mismatches) : Collections.emptyList();
    }

    @ApiStatus.Internal
    private Partition(List<E> matches, List<E> mismatches, boolean fastPath) {
        this.matches = matches;
        this.mismatches = mismatches;
    }

    public Partition(Collection<E> list, Predicate<? super E> predicate) {
        int initialCapacity = list.size();
        List<E> matchedList = new ArrayList<>(initialCapacity);
        List<E> mismatchedList = new ArrayList<>(initialCapacity);
        for (E element : list) {
            if (predicate.test(element)) matchedList.add(element);
            else mismatchedList.add(element);
        }
        this.matches = matchedList;
        this.mismatches = mismatchedList;
    }

    @Contract("_, _ -> new")
    public static <T> Partition<T> toPartition(@Nullable Collection<? extends T> matches,
                                               @Nullable Collection<? extends T> mismatches) {
        return new Partition<>(matches, mismatches);
    }

    public Collection<E> matches() {
        return Collections.unmodifiableList(matches);
    }

    public <C extends Collection<E>> C matches(Supplier<C> factory) {
        if (factory == null) return (C) new ArrayList<>(matches);
        C collection = factory.get();
        collection.addAll(matches);
        return collection;
    }

    public Collection<E> unmatches() {
        return Collections.unmodifiableList(mismatches);
    }

    public <C extends Collection<E>> C unmatches(Supplier<C> factory) {
        if (factory == null) return (C) new ArrayList<>(mismatches);
        C collection = factory.get();
        collection.addAll(mismatches);
        return collection;
    }

    @Override
    public @NotNull Iterator<E> iterator() {
        return new Iterator<>() {
            private final Iterator<E> matchIt = matches.iterator();
            private final Iterator<E> mismatchIt = mismatches.iterator();

            @Override
            public boolean hasNext() {
                return matchIt.hasNext() || mismatchIt.hasNext();
            }

            @Override
            public E next() {
                if (matchIt.hasNext()) return matchIt.next();
                if (mismatchIt.hasNext()) return mismatchIt.next();
                throw new NoSuchElementException();
            }
        };
    }

    public Collection<E> values() {
        return toList();
    }

    public List<E> toList() {
        List<E> combined = new ArrayList<>(size());
        combined.addAll(matches);
        combined.addAll(mismatches);
        return combined;
    }

    public void forEach(Consumer<? super E> action) {
        if (action == null) return;
        matches.forEach(action);
        mismatches.forEach(action);
    }

    @Contract("-> new")
    public Partition<E> swap() {
        return new Partition<>(this.mismatches, this.matches, true);
    }

    public Partition<E> matches(Consumer<? super E> action) {
        if (action != null) this.matches.forEach(action);
        return this;
    }

    public Partition<E> unmatches(Consumer<? super E> action) {
        if (action != null) this.mismatches.forEach(action);
        return this;
    }

    public double matchRatio() {
        if (isEmpty()) return 0.0;
        return (double) matches.size() / size();
    }

    @Override
    public <T> T[] toArray(@NotNull IntFunction<T[]> generator) {
        T[] array = generator.apply(size());
        int i = 0;
        for (int j = 0; j < matches.size(); j++) array[i++] = (T) matches.get(j);
        for (int j = 0; j < mismatches.size(); j++) array[i++] = (T) mismatches.get(j);
        return array;
    }

    public @NotNull Stream<E> stream() {return Stream.concat(matches.stream(), mismatches.stream());}
    public @NotNull Stream<E> parallelStream() {return stream().parallel();}
    public int size() {return matches.size() + mismatches.size();}
    public boolean isEmpty() {return matches.isEmpty() && mismatches.isEmpty();}
}
