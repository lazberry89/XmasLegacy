package xmaslegacy.Cosmetics;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Alert;
import xmaslegacy.Annotation.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Commands(command = "cos")
public class CosmeticsCommand implements CommandExecutor, TabCompleter {
	private final @NotNull CosmeticManager CSM;

	public CosmeticsCommand() {
		this.CSM = CosmeticManager.INSTANCE;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(commandSender instanceof Player p)) return true;
		if (args.length == 2) {
			Cosmetics cosmetic = CSM.getEquippedCosmetics(args[1]);

			switch (args[0].toLowerCase()) {
				case "add" -> {
					if (CSM.getEquippedCosmetics(args[1]) != null) {
						p.sendMessage(ColorUtils.chat(Alert.RED + " 이미 등록된 코스메틱입니다!"));
						return true;
					}
					ItemStack model = p.getInventory().getItemInMainHand();
					if (model.getType().isAir()) {
						p.sendMessage(ColorUtils.chat(Alert.RED + " 손에 아이템을 들고 명령어를 입력해주세요!"));
					} else {
						CSM.addCosmetics(model, args[1]);
						p.sendMessage(ColorUtils.chat(Alert.GREEN + " 아이템이 " + args[1] + " 코스메틱으로 등록되었습니다!"));
					}
					return true;
				}
				case "remove" -> {
					if (cosmetic == null) {
						p.sendMessage(ColorUtils.chat(Alert.RED + " 등록된 코스메틱이 아닙니다!"));
						return true;
					}
					cosmetic.unequip(p);
					CSM.deleteCosmetics(cosmetic);
					p.sendMessage(ColorUtils.chat(Alert.GREEN + " 코스메틱이 제거되었습니다!"));
				}
				case "equip" -> {
					if (cosmetic == null) {
						p.sendMessage(ColorUtils.chat(Alert.RED + " 등록된 코스메틱이 아닙니다!"));
						return true;
					}
					cosmetic.equip(p);
					p.sendMessage(ColorUtils.chat(Alert.GREEN + " 코스메틱이 장착되었습니다!"));
				}
				case "unequip" -> {
					if (cosmetic == null) {
						p.sendMessage(ColorUtils.chat(Alert.RED + " 등록된 코스메틱이 아닙니다!"));
						return true;
					}
					cosmetic.unequip(p);
					p.sendMessage(ColorUtils.chat(Alert.GREEN + " 코스메틱이 해제되었습니다!"));
				}
			}
		} else {
			p.sendMessage(ColorUtils.chat(Alert.RED + " 잘못된 명령어입니다!"));
			return true;
		}
		return true;
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		List<String> result = new ArrayList<>();
		if (args.length == 1) {
			String[] subCommands = {"add", "remove", "equip", "unequip"};
			result.addAll(Arrays.asList(subCommands));
			return result;
		} else if (args.length == 2) {
			result.addAll(CSM.getCosmeticsName());
		}
		return result;
	}
}
