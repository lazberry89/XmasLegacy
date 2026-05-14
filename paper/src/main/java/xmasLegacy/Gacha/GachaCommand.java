package xmasLegacy.Gacha;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.settings.Prefix;
import xmasLegacy.InfoLevel;
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GachaCommand implements CommandExecutor, TabCompleter {
    private final XmasLegacy plugin;
    private final GachaManager gm;

    public GachaCommand(XmasLegacy plugin) {
        this.plugin = plugin;
        this.gm = plugin.GM;
    }

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull ... args) {
		if (!(sender instanceof Player p)) return true;

		if (args.length == 0) {
			error(p, "사용법: /gacha <add | remove> ...");
			return true;
		}
		if (args.length == 1 && args[0].equalsIgnoreCase("inv") || args[0].equalsIgnoreCase("inventory")) {
			p.openInventory(new BundleTypeInterface(plugin).getInventory());
			p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
			return true;
		}
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("bundle")) {
				switch (args[1]) {
					case "bundle" -> p.getInventory().addItem(gm.Bundle());
					case "high_end" -> p.getInventory().addItem(gm.HighEndBundle());
					case "chromatic_bundle" -> p.getInventory().addItem(gm.ChromaticBundle());
					case "chromatic_box" -> p.getInventory().addItem(gm.ChromaticBox());
					default -> plugin.infoMsg(InfoLevel.ERROR, p, "번들 종류는 bundle, high_end, chromatic_bundle, chromatic_box 중 하나여야 합니다! (예시: /gacha bundle bundle)");
				}
			}
		}

		String action = args[0].toLowerCase();

		if (action.equalsIgnoreCase("add") && args.length >= 5) {
			String key = args[1];
			GachaGrade grade;
			double chance;
			List<BundleType> types = new ArrayList<>();

			try {
				grade = GachaGrade.valueOf(args[2].toUpperCase()); // args[2]는 등급
				chance = Double.parseDouble(args[3]);             // args[3]은 확률
				for (int i = 4; i < args.length; i++) {           // args[4]부터는 모두 타입
					types.add(BundleType.valueOf(args[i].toUpperCase()));
				}
			} catch (IllegalArgumentException e) {
				error(p, "인자 값이 올바르지 않습니다. (순서: 이름 등급 확률 타입)");
				return true;
			}

			ItemStack item = p.getInventory().getItemInMainHand();
			if (item.getType().isAir()) {
				error(p, "손에 아이템을 들고 있어야 합니다!");
				return true;
			}

			for (BundleType type : types) {
				gm.addGacha(key, item, grade, chance, type);
			}
			p.sendMessage(ColorUtils.chat(Prefix.GREEN + " 가챠 아이템 등록 완료: " + key));
			p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			return true;
		}

		// 2. REMOVE 로직 (인자 3개: remove <이름> <타입>)
		else if (action.equalsIgnoreCase("remove") && args.length == 3) {
			String key = args[1];
			try {
				BundleType type = BundleType.valueOf(args[2].toUpperCase());
				if (gm.removeGacha(key, type)) {
					p.sendMessage(ColorUtils.chat(Prefix.GREEN + " 아이템을 삭제하였습니다."));
					p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1.0f, 1.0f);
				} else {
					error(p, "해당 타입에 존재하지 않는 아이템 이름입니다.");
				}
			} catch (IllegalArgumentException e) {
				error(p, "유효하지 않은 번들 타입입니다!");
			}
			return true;
		}

		error(p, "사용법: \n&7/gacha add <이름> <등급> <확률> <타입...>\n&7/gacha remove <이름> <타입>");
		return true;
	}


    private void error(Player p, String msg) {
        p.sendMessage(ColorUtils.chat(Prefix.RED + " " + msg));
        p.playSound(p, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
    }

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
		List<String> result = new ArrayList<>();
		String action = args[0].toLowerCase();

		if (args.length == 1) {
			result.addAll(List.of("add", "remove", "inv", "inventory", "bundle"));
		} else if (action.equals("add")) {
			switch (args.length) {
				case 2 -> result.add("<이름>");
				case 3 -> Arrays.stream(GachaGrade.values()).map(Enum::name).forEach(result::add); // 등급 먼저
				case 4 -> result.add("0.0001"); // 확률 예시
				default -> Arrays.stream(BundleType.values()).map(Enum::name).forEach(result::add); // 5번부터는 타입
			}
		} else if (action.equals("remove")) {
			if (args.length == 2) gm.getAll().forEach(gacha -> result.add(gacha.getKey()));
			if (args.length == 3) Arrays.stream(BundleType.values()).map(Enum::name).forEach(result::add);
		} else if (action.equalsIgnoreCase("bundle") && args.length == 2) {
			result.addAll(List.of("bundle", "high_end", "chromatic_bundle", "chromatic_box"));
		}

		// 현재 입력 중인 단어로 필터링하여 반환
		String lastArg = args[args.length - 1].toLowerCase();
		return result.stream()
				.filter(s -> s.toLowerCase().startsWith(lastArg))
				.toList();
	}
}
