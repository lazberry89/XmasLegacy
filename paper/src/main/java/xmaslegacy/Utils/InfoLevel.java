package xmaslegacy.Utils;

import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.settings.Alert;

public enum InfoLevel {
    ERROR(Alert.RED, Sound.BLOCK_NOTE_BLOCK_BASS),
    WARN(Alert.YELLOW, Sound.BLOCK_NOTE_BLOCK_BASS),
    INFO(Alert.GREEN, Sound.ENTITY_ARROW_HIT_PLAYER);

    private final @NotNull Alert prefix;
    private final @NotNull Sound sound;

    InfoLevel(@NotNull Alert prefix, @NotNull Sound sound) {
        this.prefix = prefix;
        this.sound = sound;
    }

    public @NotNull Alert prefix() {
        return this.prefix;
    }
    public @NotNull Sound sound() {
        return this.sound;
    }
}
