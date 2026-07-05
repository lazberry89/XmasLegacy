package org.lazberry.xmaslegacy.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

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

		try {
			return (T) value;
		} catch (ClassCastException e) {
			return def;
		}
	}

	/**
	 * This method is only for getting float value.
	 * <pre>{@code
	 * config.getFloat("a,a", 1.5f);
	 * }</pre>
	 * @param key key to target value
	 * @param def float value to use as default.
	 * @return float value of key. Always cast value to float if value is number whatever the value was.
	 */
	public float getFloat(String key, float def) {
		Object value = this.file.get(key);
		if (value instanceof Number num) {
			return num.floatValue();
		}
		return def;
	}

	/**
	 * This method is only for getting long value.
	 * <pre>{@code
	 * config.getLong("b.b", 100L);
	 * }</pre>
	 * @param key key to target value
	 * @param def long value to use as default.
	 * @return long value of key. Always cast value to long if value is number whatever the value was.
	 */
	public long getLong(String key, long def) {
		Object value = this.file.get(key);
		if (value instanceof Number num) {
			return num.longValue(); // 💡 Integer -> long 완벽 변환
		}
		return def;
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