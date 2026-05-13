package xmasLegacy;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.settings.Prefix;

public enum InfoLevel {
    ERROR(Prefix.RED, Sound.BLOCK_NOTE_BLOCK_BASS),
    WARN(Prefix.YELLOW, Sound.BLOCK_NOTE_BLOCK_BASS),
    INFO(Prefix.GREEN, Sound.ENTITY_ARROW_HIT_PLAYER);

    private final Prefix prefix;
    private final Sound sound;

    InfoLevel(Prefix prefix, Sound sound) {
        this.prefix = prefix;
        this.sound = sound;
    }

    public Prefix Prefix() {
        return this.prefix;
    }
    public Sound Sound() {
        return this.sound;
    }
}
