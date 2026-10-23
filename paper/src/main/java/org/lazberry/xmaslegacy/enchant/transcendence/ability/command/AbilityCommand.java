package org.lazberry.xmaslegacy.enchant.transcendence.ability.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.Ability;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityManager;
import org.lazberry.xmaslegacy.enchant.transcendence.ability.AbilityType;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Registry;
import org.lazberry.xmaslegacy.utils.InventoryHelper;
import org.lazberry.xmaslegacy.utils.ParseEnum;

import java.util.List;

@Commands(command = "ability")
@Registry.Exclude(type = ServerType.LOBBY)
public class AbilityCommand implements CommandExecutor, TabCompleter {
	private final AbilityManager abilityManager;

	@Inject
	public AbilityCommand(AbilityManager abilityManager) {
		this.abilityManager = abilityManager;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		if (args.length >= 1) {
			String name = args[0].toUpperCase();
			AbilityType type = ParseEnum.of(AbilityType.class).parseOrDefault(name, AbilityType.SonicHunter);
			Ability ability = abilityManager.getAbility(type);
			InventoryHelper.giveItemOrDrop(p, ability.weapon());
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		return List.of();
	}
}
