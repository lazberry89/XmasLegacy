package org.lazberry.xmaslegacy.bags;

import java.util.UUID;
import org.lazberry.xmaslegacy.utils.Perms;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Commands;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.ServerType;

import java.util.List;

@Commands(command = "가방")
@Components.Exclude(type = ServerType.LOBBY)
public class BagCommand implements CommandExecutor, TabCompleter {
	private final @NotNull BagManager bm;

	@Inject
	public BagCommand(@NotNull BagManager bm) {
		this.bm = bm;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull ...args) {
		if (!(commandSender instanceof Player p)) return false;
		if (args.length == 0) {
			openBag(p, p.getUniqueId());
			return true;
		} else if (args.length == 1) {
			if (!Perms.admin(p, "bags")) {
				p.sendMessage(ColorUtils.chat(Alert.RED + " 권한이 없습니다!"));
				p.playSound(p, Sound.BLOCK_ANVIL_LAND, 0.3f, 1.0f);
				return true;
			} else {
				// Online only: an offline bag is not loaded here, and edits to it would be lost.
				Player target = Bukkit.getPlayerExact(args[0]);
				if (target == null) {
					p.sendMessage(ColorUtils.chat(Alert.RED + " 이 서버에 접속 중인 플레이어가 아닙니다."));
					return true;
				}
				if (openBag(p, target.getUniqueId())) {
					p.sendMessage(ColorUtils.chat(Alert.YELLOW + " 플레이어 &6" + target.getName() + "&f의 가방을 조회중입니다."));
					p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
					return true;
				}
			}

		} else {
			p.sendMessage(ColorUtils.chat("&cUsage: &7/bag"));
		}
		return true;
	}

	private boolean openBag(Player viewer, UUID owner) {
		Bag bag = bm.getBag(owner);
		if (bag == null) {
			viewer.sendMessage(ColorUtils.chat(Alert.RED + " 가방을 불러오는 중입니다. 잠시 후 다시 시도해주세요."));
			return false;
		}
		viewer.openInventory(bag.getInventory());
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {
		if (strings.length == 1) {
			if (!Perms.admin(commandSender, "bags")) return List.of();
			return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
		}
		return List.of();
	}
}
