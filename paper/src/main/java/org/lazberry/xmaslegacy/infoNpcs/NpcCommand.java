package org.lazberry.xmaslegacy.infoNpcs;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.Annotation.Commands;
import org.lazberry.xmaslegacy.PlayerUtils.BagManager;
import org.lazberry.xmaslegacy.utils.InfoUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

import java.util.Arrays;
import java.util.List;

@Commands(command = "guide")
@Registry.Exclude(type = ServerType.LOBBY)
public class NpcCommand implements CommandExecutor, TabCompleter {
    private final @NotNull NpcManager ncm;
	private final @NotNull BagManager bm;

	@Inject
    public NpcCommand(@NotNull NpcManager ncm, @NotNull BagManager bm) {
		this.ncm = ncm;
		this.bm = bm;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		if (!p.isOp()) return true;
		if (args.length == 1) {
			AbstractNpc npc;
			try {
				NpcType type = NpcType.valueOf(args[0].toUpperCase());
				npc = ncm.getNpcInstance(type);
				npc.sendCaption(p, bm);
			} catch (IllegalArgumentException e) {
				InfoUtils.error(p, "등록되지 않은 가이드이거나 잘못된 명령어입니다.");
				return true;
			}
		} else return true;
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        return List.of(Arrays.stream(NpcType.values()).map(NpcType::name).toArray(String[]::new));
    }
}
