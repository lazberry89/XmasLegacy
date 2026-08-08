package org.lazberry.xmaslegacy.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import javax.annotation.ParametersAreNonnullByDefault;

@TestOnly
@ParametersAreNonnullByDefault
public record Config(FileConfiguration file) {

	/**
	 * Static method of returning chaining method of Config.
	 * <pre>{@code
	 * Config.of(file); // Starts chaining from here
	 * }</pre>
	 * @param file File Configuration of target information
	 * @return Instance of Config builder
	 */
	public static @NotNull Config of(FileConfiguration file) {
		return new Config(file);
	}

	/**
	 * Programmatically sets a target key-value pair directly into the configuration stream.
	 *
	 * @param key   The absolute configuration path signature (e.g., "fields.arena1.max").
	 * @param value The target object asset to write into the YAML memory node.
	 * @return The current operational {@link Config} context instance to support continuous API chaining.
	 */
	@Contract("_, _ -> this")
	public @NotNull Config set(String key, @Nullable Object value) {
		this.file.set(key, value);
		return this;
	}

	/**
	 * this method sets default value to key to target Config.
	 * @param key Key of value
	 * @param value target value of Key
	 * @return chaining instance of Config builder
	 */
	@Contract("_, _ -> this")
	public @NotNull Config setDefault(String key, Object value) {
		this.file.addDefault(key, value);
		return this;
	}

	/**
	 * This method returns cast value of default. if not exist, returns default.
	 * <pre>{@code
	 * Integer a = config.getValue("a.a", 1);
	 * String b = config.getValue("b.b", "B");
	 * int c = config.getValue("c.c", 10); //Existing default value, the returned value NEVER be null.
	 * }</pre>
	 * @param key Key of value
	 * @param def default value
	 * @param <T> get generic value of default and casts return to value.
	 * @return return cast value of default one.
	 */
	@SuppressWarnings("unchecked")
	public @NotNull <T> T getValue(String key, T def) {
		Object value = this.file.get(key);
		if (value == null) return def;

		if (value instanceof Number num) {
            switch (def) {
                case Float ignored -> {return (T) Float.valueOf(num.floatValue());}
                case Long ignored -> {return (T) Long.valueOf(num.longValue());}
                case Integer ignored -> {return (T) Integer.valueOf(num.intValue());}
                case Double ignored -> {return (T) Double.valueOf(num.doubleValue());}
                default -> {}
            }
        }
		try {
			return (T) value;
		} catch (ClassCastException e) {
			return def;
		}
	}

	/**
	 * This method returns cast return value, but if value is null, returns null.
	 * <pre>{@code
	 * Location loc = config.getValue("teleport.spawn");
	 * ItemStack item = config.getValue("custom.item");
	 * }</pre>
	 * @param key Key to target value
	 * @param <T> generic type
	 * @return returns target value, but returns null if not exists.
	 */
	@SuppressWarnings("unchecked")
	public @Nullable <T> T getValue(String key) {
		Object value = this.file.get(key);
		if (value == null) return null;
		try {
			return (T) value;
		} catch (ClassCastException e) {
			return null;
		}
	}
}