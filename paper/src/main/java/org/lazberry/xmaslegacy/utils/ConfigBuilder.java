package org.lazberry.xmaslegacy.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;

@ParametersAreNonnullByDefault
public record ConfigBuilder(FileConfiguration file) {

	public static @NotNull ConfigBuilder create() {
		return new ConfigBuilder(new YamlConfiguration());
	}

	public static @NotNull ConfigBuilder of(File file) {
		return new ConfigBuilder(YamlConfiguration.loadConfiguration(file));
	}

	public static @NotNull ConfigBuilder of(FileConfiguration file) {
		return new ConfigBuilder(file);
	}

	@Contract("_, _ -> this")
	public @NotNull ConfigBuilder set(String key, @Nullable Object value) {
		this.file.set(key, value);
		return this;
	}

	@Contract("_, _ -> this")
	public @NotNull ConfigBuilder setDefault(String key, Object value) {
		if (!this.file.isSet(key)) this.file.set(key, value);
		return this;
	}

	@SuppressWarnings("unchecked")
	public @NotNull <T> T getValue(String key, T def) {
		Object value = this.file.get(key);
		if (value == null) return def;

		if (value instanceof Number num) {
			switch (def) {
				case Float ignored -> { return (T) Float.valueOf(num.floatValue()); }
				case Long ignored -> { return (T) Long.valueOf(num.longValue()); }
				case Integer ignored -> { return (T) Integer.valueOf(num.intValue()); }
				case Double ignored -> { return (T) Double.valueOf(num.doubleValue()); }
				case Short ignored -> { return (T) Short.valueOf(num.shortValue()); }
				default -> {}
			}
		}
		if (def.getClass().isInstance(value)) return (T) value;
		return def;
	}

	public @Nullable <T> T getValue(String key, Class<T> type) {
		Object value = this.file.get(key);
		if (type.isInstance(value)) return type.cast(value);
		return null;
	}

	@Contract("_ -> this")
	public @NotNull ConfigBuilder save(File targetFile) {
		try {
			this.file.save(targetFile);
		} catch (IOException e) {
			throw new RuntimeException("Failed to save config to " + targetFile, e);
		}
		return this;
	}

	public @NotNull YamlConfiguration build() {
		if (this.file instanceof YamlConfiguration yaml) {
			return yaml;
		}
		throw new IllegalStateException("Underlying configuration is not a YamlConfiguration instance.");
	}
}