package org.lazberry.xmaslegacy.utils;

import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;

/**
 * Admin checks per feature: {@code xmaslegacy.admin.<feature>} (e.g. xmaslegacy.admin.casino).
 * Ops keep every node because Bukkit defaults unregistered permissions to op. To hand out admin rights
 * without op, grant nodes in LuckPerms; {@code xmaslegacy.admin.*} gives all of them.
 */
public final class Perms {
	public static final String ADMIN = "xmaslegacy.admin";

	private Perms() {}

	public static boolean admin(@NotNull Permissible who, @NotNull String feature) {
		return who.hasPermission(ADMIN + "." + feature);
	}
}
