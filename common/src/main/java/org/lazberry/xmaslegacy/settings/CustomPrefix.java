package org.lazberry.xmaslegacy.settings;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;

@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CustomPrefix implements ServerPrefix {
    @EqualsAndHashCode.Include
    private final @NotNull String name;
    @EqualsAndHashCode.Exclude
    private final @NotNull Component prefix;
    private @Nullable @Getter @Setter Component description;

    public CustomPrefix(@NotNull String name, @NotNull Component prefix) {
        this.name = name;
        this.prefix = prefix;
        log.info("Prefix {} created.", name);
    }

    public CustomPrefix(@NotNull String name) {
        this.name = name;
        this.prefix = ColorUtils.chat(name);
        log.info("Prefix {} created without any custom design.", name);
    }

    @Override
    public @NotNull Component prefix() {
        return this.prefix;
    }

    @Override
    public @NotNull String name() {
        return this.name;
    }

    /**
     * For enum prefixes. Not Used here.
     * @return 0
     */
    @Override
    public int ordinal() {
        return 0;
    }
}
