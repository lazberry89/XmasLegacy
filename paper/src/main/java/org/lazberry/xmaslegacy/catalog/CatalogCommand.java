package org.lazberry.xmaslegacy.catalog;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Commands;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.utils.ParseEnum;
import org.lazberry.xmaslegacy.utils.Perms;

import java.util.List;
import java.util.stream.Stream;

/** /도감 opens the catalogs; /도감 샘플 &lt;type&gt; edits a catalog's items (admins). */
@Commands(command = "도감")
@Components.Exclude(type = ServerType.LOBBY)
public class CatalogCommand implements CommandExecutor, TabCompleter {
	private final CatalogManager manager;

	@Inject
	public CatalogCommand(CatalogManager manager) {
		this.manager = manager;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		if (args.length == 0) {
			manager.openSelection(p);
			return true;
		}
		if (!args[0].equals("샘플") || !Perms.admin(p, "catalog")) {
			InfoUtils.error(p, "사용법: /도감");
			return true;
		}
		CatalogType type = args.length >= 2 ? ParseEnum.of(CatalogType.class).parse(args[1].toUpperCase()) : null;
		if (type == null) InfoUtils.error(p, "사용법: /도감 샘플 <{}>", String.join(" | ", typeNames().toList()));
		else manager.openSampleEditor(p, type);
		return true;
	}

	private static Stream<String> typeNames() {
		return Stream.of(CatalogType.values()).map(CatalogType::name);
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!Perms.admin(sender, "catalog")) return List.of();
		return switch (args.length) {
			case 1 -> List.of("샘플");
			case 2 -> typeNames().filter(n -> n.startsWith(args[1].toUpperCase())).toList();
			default -> List.of();
		};
	}
}
