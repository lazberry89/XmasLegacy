package xmaslegacy.Utils;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.settings.Alert;

public enum InfoLevel {
    ERROR(Alert.RED, Sound.BLOCK_NOTE_BLOCK_BASS),
    WARN(Alert.YELLOW, Sound.BLOCK_NOTE_BLOCK_BASS),
    INFO(Alert.GREEN, Sound.ENTITY_ARROW_HIT_PLAYER);

    private final Alert prefix;
    private final Sound sound;

    InfoLevel(Alert prefix, Sound sound) {
        this.prefix = prefix;
        this.sound = sound;
    }

    public Alert Prefix() {
        return this.prefix;
    }
    public Sound Sound() {
        return this.sound;
    }
}
