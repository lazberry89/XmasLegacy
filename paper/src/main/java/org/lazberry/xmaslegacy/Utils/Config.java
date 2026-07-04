package org.lazberry.xmaslegacy.Utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public record Config(FileConfiguration file) {

	/**
	 * Static method of returning chaining method of Config.
	 * @param file File Configuration of target information
	 * @return Instance of Config builder
	 * <pre>{@code
	 * Config.of(file); //Starts chaining from here
	 * }</pre>
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
}
