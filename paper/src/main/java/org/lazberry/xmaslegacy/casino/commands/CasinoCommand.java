package org.lazberry.xmaslegacy.casino.commands;

import org.lazberry.xmaslegacy.utils.Perms;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.LazberryRegistryFramework.annotation.Commands;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.casino.games.CoinFlip.CoinFlipManager;
import org.lazberry.xmaslegacy.casino.games.SlotMachine.SlotMachine;
import org.lazberry.xmaslegacy.casino.games.SlotMachine.SlotMachineManager;
import org.lazberry.xmaslegacy.casino.logics.CasinoConfig;
import org.lazberry.xmaslegacy.casino.shop.CasinoShop;
import org.lazberry.xmaslegacy.casino.versus.VersusManager;
import org.lazberry.xmaslegacy.framework.ServerType;
import org.lazberry.xmaslegacy.framework.annotation.Components;
import org.lazberry.xmaslegacy.framework.annotation.Inject;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Players: /casino shop | join | leave.
 * Ops:     /casino slot | coinflip | versus  (setup; every change is saved to casino.yml).
 */
@Commands(command = "casino")
@Components.Include(type = ServerType.MAIN)
public class CasinoCommand implements CommandExecutor, TabCompleter {
	private static final List<String> PLAYER_SUBS = List.of("shop", "join", "leave");
	private static final List<String> ADMIN_SUBS = List.of("slot", "coinflip", "versus");

	private final CasinoShop shop;
	private final SlotMachineManager slots;
	private final CoinFlipManager coinFlips;
	private final VersusManager versus;
	private final CasinoConfig config;

	@Inject
	public CasinoCommand(XmasLegacy plugin, SlotMachineManager slots, CoinFlipManager coinFlips,
	                     VersusManager versus, CasinoConfig config) {
		this.shop = new CasinoShop(plugin);
		this.slots = slots;
		this.coinFlips = coinFlips;
		this.versus = versus;
		this.config = config;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if (!(sender instanceof Player p)) return true;
		String sub = args.length > 0 ? args[0].toLowerCase() : "";

		switch (sub) {
			case "shop" -> p.openInventory(shop.getInventory());
			case "join" -> versus.join(p);
			case "leave" -> versus.leave(p);
			case "slot", "coinflip", "versus" -> {
				if (!Perms.admin(p, "casino")) {
					InfoUtils.error(p, "권한이 없습니다.");
					return true;
				}
				switch (sub) {
					case "slot" -> slot(p, args);
					case "coinflip" -> coinFlip(p, args);
					default -> versus(p, args);
				}
			}
			default -> help(p);
		}
		return true;
	}

	private void help(Player p) {
		InfoUtils.info(p, "/casino shop &7- 입장권, 코인 구매");
		InfoUtils.info(p, "/casino join | leave &7- 1대1 대결 참가 / 퇴장");
		if (!Perms.admin(p, "casino")) return;
		InfoUtils.info(p, "/casino slot create <id> <코인> <은배수> <금배수> <다이아배수> &7- 바라보는 레버");
		InfoUtils.info(p, "/casino slot reel <id> <1|2|3> &7- 바라보는 블록 | remove <id> | list");
		InfoUtils.info(p, "/casino coinflip create <id> &7- 바라보는 블록 | remove <id> | list");
		InfoUtils.info(p, "/casino versus set <blue|red|blueroom|redroom|entrance> &7- 현재 위치");
		InfoUtils.info(p, "/casino versus fence <add|remove|clear> &7- 바라보는 블록 | info");
	}

	/* ---------- slot machines ---------- */

	private void slot(Player p, String[] args) {
		String action = arg(args, 1);
		String id = arg(args, 2);
		switch (action) {
			case "create" -> {
				if (args.length < 7) {
					InfoUtils.error(p, "/casino slot create <id> <코인> <은배수> <금배수> <다이아배수>");
					return;
				}
				if (slots.getSlotMachine(id).isPresent()) {
					InfoUtils.error(p, "이미 있는 슬롯머신 id입니다.");
					return;
				}
				Block lever = target(p);
				if (lever == null || lever.getType() != Material.LEVER) {
					InfoUtils.error(p, "레버를 바라보고 입력하세요.");
					return;
				}
				try {
					int cost = Integer.parseInt(args[3]);
					double silver = Double.parseDouble(args[4]), gold = Double.parseDouble(args[5]), diamond = Double.parseDouble(args[6]);
					if (cost <= 0 || silver < 0 || gold < 0 || diamond < 0) throw new NumberFormatException();
					SlotMachine m = slots.createSlotMachine(id, lever.getLocation(), cost, silver, gold, diamond);
					config.saveSlot(m);
					InfoUtils.info(p, "슬롯머신 &6{}&f 생성. 이제 /casino slot reel {} 1~3 으로 릴 위치를 지정하세요.", id, id);
				} catch (NumberFormatException e) {
					InfoUtils.error(p, "코인은 1 이상의 정수, 배수는 0 이상의 숫자여야 합니다.");
				}
			}
			case "reel" -> slots.getSlotMachine(id).ifPresentOrElse(m -> {
				int index = parseInt(arg(args, 3));
				Block block = target(p);
				if (index < 1 || index > 3 || block == null) {
					InfoUtils.error(p, "블록을 바라보고 /casino slot reel <id> <1|2|3>");
					return;
				}
				m.setReel(index, block.getLocation());
				config.saveSlot(m);
				InfoUtils.info(p, "{}번 릴 위치 지정 완료.{}", index, m.isReady() ? " &a슬롯머신 사용 가능!" : "");
			}, () -> InfoUtils.error(p, "없는 슬롯머신입니다."));
			case "remove" -> {
				if (slots.removeSlotMachine(id)) {
					config.removeSlot(id);
					InfoUtils.info(p, "슬롯머신 &6{}&f 삭제.", id);
				} else InfoUtils.error(p, "없는 슬롯머신입니다.");
			}
			case "list" -> slots.getMachines().values().forEach(m -> InfoUtils.info(p, "&6{}&f: 코인 {}개, {}",
					m.getName(), m.getNeededCoin(), m.isReady() ? "&a사용 가능" : "&c릴 미지정"));
			default -> help(p);
		}
	}

	/* ---------- coin flips ---------- */

	private void coinFlip(Player p, String[] args) {
		String action = arg(args, 1);
		String id = arg(args, 2);
		switch (action) {
			case "create" -> {
				Block block = target(p);
				if (id.isEmpty() || block == null) {
					InfoUtils.error(p, "블록을 바라보고 /casino coinflip create <id>");
					return;
				}
				if (coinFlips.getMachines().containsKey(id)) {
					InfoUtils.error(p, "이미 있는 id입니다.");
					return;
				}
				Location loc = block.getLocation();
				coinFlips.registerMachine(id, loc, Casino.coin(1));
				config.saveCoinFlip(id, loc);
				InfoUtils.info(p, "동전 던지기 &6{}&f 생성. 블록을 우클릭하면 플레이할 수 있습니다.", id);
			}
			case "remove" -> {
				if (coinFlips.removeMachine(id)) {
					config.removeCoinFlip(id);
					InfoUtils.info(p, "동전 던지기 &6{}&f 삭제.", id);
				} else InfoUtils.error(p, "없는 id입니다.");
			}
			case "list" -> coinFlips.getMachines().forEach((key, m) -> InfoUtils.info(p, "&6{}&f: {} {} {}",
					key, m.getLocation().getBlockX(), m.getLocation().getBlockY(), m.getLocation().getBlockZ()));
			default -> help(p);
		}
	}

	/* ---------- versus arena ---------- */

	private void versus(Player p, String[] args) {
		switch (arg(args, 1)) {
			case "set" -> {
				String point = arg(args, 2);
				if (!CasinoConfig.VERSUS_POINTS.contains(point)) {
					InfoUtils.error(p, "/casino versus set <blue|red|blueroom|redroom|entrance>");
					return;
				}
				List<String> missing = config.setVersusPoint(point, p.getLocation());
				if (missing.isEmpty()) InfoUtils.info(p, "&6{}&f 지정 완료. &a경기장 사용 가능!", point);
				else InfoUtils.info(p, "&6{}&f 지정 완료. 남은 위치: {}", point, String.join(", ", missing));
			}
			case "fence" -> versus.getField().ifPresentOrElse(f -> {
				String action = arg(args, 2);
				Block block = target(p);
				switch (action) {
					case "add", "remove" -> {
						if (block == null) {
							InfoUtils.error(p, "울타리 블록을 바라보고 입력하세요.");
							return;
						}
						boolean changed = action.equals("add") ? f.addFence(block.getLocation()) : f.removeFence(block.getLocation());
						if (changed && action.equals("add")) f.setRestoreMaterial(block.getType());
						InfoUtils.info(p, changed ? "적용했습니다. (울타리 {}개)" : "변경 사항이 없습니다. (울타리 {}개)", f.getBreakableFences().size());
					}
					case "clear" -> {
						f.getBreakableFences().clear();
						InfoUtils.info(p, "울타리를 모두 지웠습니다.");
					}
					default -> {
						help(p);
						return;
					}
				}
				config.saveFences();
			}, () -> InfoUtils.error(p, "경기장 위치 5개를 먼저 모두 지정하세요. 남은 위치: {}",
					String.join(", ", config.missingVersusPoints())));
			case "info" -> {
				List<String> missing = config.missingVersusPoints();
				InfoUtils.info(p, missing.isEmpty() ? "경기장: &a사용 가능" : "경기장: &c미완성 &f(남은 위치: " + String.join(", ", missing) + ")");
				versus.getField().ifPresent(f -> InfoUtils.info(p, "울타리 {}개, 경기 중: {}", f.getBreakableFences().size(), f.isRunning()));
			}
			default -> help(p);
		}
	}

	/* ---------- helpers ---------- */

	private static @Nullable Block target(Player p) {
		Block block = p.getTargetBlockExact(6);
		return block == null || block.getType().isAir() ? null : block;
	}

	private static String arg(String[] args, int index) {
		return args.length > index ? args[index].toLowerCase() : "";
	}

	private static int parseInt(String s) {
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		boolean op = Perms.admin(sender, "casino");
		List<String> options = new ArrayList<>();
		if (args.length == 1) {
			options.addAll(PLAYER_SUBS);
			if (op) options.addAll(ADMIN_SUBS);
		} else if (op && args.length == 2) {
			switch (args[0].toLowerCase()) {
				case "slot", "coinflip" -> options.addAll(List.of("create", "remove", "list"));
				case "versus" -> options.addAll(List.of("set", "fence", "info"));
			}
			if (args[0].equalsIgnoreCase("slot")) options.add("reel");
		} else if (op && args.length == 3) {
			switch (args[0].toLowerCase() + " " + args[1].toLowerCase()) {
				case "slot reel", "slot remove" -> options.addAll(slots.getMachines().keySet());
				case "coinflip remove" -> options.addAll(coinFlips.getMachines().keySet());
				case "versus set" -> options.addAll(CasinoConfig.VERSUS_POINTS);
				case "versus fence" -> options.addAll(List.of("add", "remove", "clear"));
			}
		} else if (op && args.length == 4 && args[0].equalsIgnoreCase("slot") && args[1].equalsIgnoreCase("reel")) {
			options.addAll(List.of("1", "2", "3"));
		}
		String typed = args[args.length - 1].toLowerCase();
		return options.stream().filter(o -> o.toLowerCase().startsWith(typed)).toList();
	}
}
