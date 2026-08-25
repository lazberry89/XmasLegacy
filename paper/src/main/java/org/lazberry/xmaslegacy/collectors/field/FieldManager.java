package org.lazberry.xmaslegacy.collectors.field;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.lazberry.xmaslegacy.collectors.game.Difficulty;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Registry.Include(type = ServerType.MAIN)
public class FieldManager {
    private final Map<Difficulty, Field> fieldByDifficulty = new ConcurrentHashMap<>(5);

    public FieldManager() {}

    public void registerField(Field field) {
        fieldByDifficulty.put(field.getDifficulty(), field);
    }

    public @Nullable Field removeField(Difficulty difficulty) {
        return fieldByDifficulty.remove(difficulty);
    }

    public Optional<Field> getField(Difficulty difficulty) {
        return Optional.ofNullable(fieldByDifficulty.get(difficulty));
    }

    public void forEach(Consumer<Field> consumer) {
        snapshot().forEach(consumer);
    }

    public void ifRegistered(Difficulty difficulty, Consumer<Field> consumer) {
        getField(difficulty).ifPresent(consumer);
    }

    public void clear() {
        fieldByDifficulty.clear();
    }

    @Unmodifiable
    public Collection<Field> snapshot() {
        return Collections.unmodifiableCollection(fieldByDifficulty.values());
    }
}
