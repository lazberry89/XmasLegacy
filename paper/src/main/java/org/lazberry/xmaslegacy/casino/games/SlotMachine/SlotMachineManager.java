package org.lazberry.xmaslegacy.casino.games.SlotMachine;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.lazberry.xmaslegacy.casino.Casino.countCoins;

public class SlotMachineManager {
	private final Map<String, SlotMachine> machines = new ConcurrentHashMap<>();
	private final Map<String, SlotAnimationController> controllers = new ConcurrentHashMap<>();
	private final Set<String> activeRunningMachines = ConcurrentHashMap.newKeySet();

	private final double diamondChance;
	private final double goldChance;
	private final double silverChance;
	private final double noLuckChance;
	private final XmasLegacy plugin;

	public SlotMachineManager(double diamondChance, double goldChance, double silverChance, double noLuckChance, XmasLegacy plugin) {
		this.diamondChance = diamondChance;
		this.goldChance = goldChance;
		this.silverChance = silverChance;
		this.noLuckChance = noLuckChance;
		this.plugin = plugin;
	}

	public Optional<SlotMachine> getSlotMachine(String id) {
		return Optional.ofNullable(machines.get(id));
	}

	public Optional<SlotAnimationController> getaMachineController(String id) {
		return Optional.ofNullable(controllers.get(id));
	}

	public void createSlotMachine(String id, Location slot1, Location slot2, Location slot3, int neededCoin,
	                              double multipliesForSilver, double multipliesForGold, double multipliesForDiamond) {
		SlotMachine newMachine = new SlotMachine(id, slot1, slot2, slot3, neededCoin, multipliesForSilver, multipliesForGold, multipliesForDiamond);
		SlotAnimationController controller = new SlotAnimationController(plugin, newMachine);
		machines.put(id, newMachine);
		controllers.put(id, controller);
	}

	public void runSlotMachine(Player player, String id) {
		var machine = machines.get(id);
		var controller = controllers.get(id);

		if (machine == null || controller == null) {
			InfoUtils.error(player, "유효하지 않은 머신입니다.");
			return;
		}

		if (!activeRunningMachines.add(id)) {
			InfoUtils.error(player, "이미 이 슬롯머신이 작동 중입니다!");
			return;
		}

		int neededCoins = machine.getNeededCoin();
		if (countCoins(player) < neededCoins) {
			InfoUtils.error(player, "카지노 코인이 부족합니다! (필요 코인: " + neededCoins + "개)");
			activeRunningMachines.remove(id);
			return;
		}
		consumeCoins(player, neededCoins);

		Symbol s1 = drawRandomSymbol();
		Symbol s2 = drawRandomSymbol();
		Symbol s3 = drawRandomSymbol();

		Runnable onComplete = resultBySymbols(player, machine, id, s1, s2, s3);

		controller.startSpin(s1, s2, s3, onComplete);
	}

	private Runnable resultBySymbols(Player player, SlotMachine machine, String machineId, Symbol slot1, Symbol slot2, Symbol slot3) {
		return () -> {
			try {
				if (slot1 == slot2 && slot2 == slot3) {
					double multiplier = machine.getMultipliesPerSymbol(slot1);
					int payoutAmount = Math.toIntExact(Math.round(machine.getNeededCoin() * multiplier));

					if (payoutAmount > 0) {
						Casino.setCoins(player, Casino.countCoins(player) + payoutAmount);
					}

					if (slot1 == Symbol.NO_LUCK) {
						Casino.sendIconAlert(player, "NO LUCK 3개 매치! 배수 없이 판돈만 돌려받습니다.");
						return;
					}

					Casino.sendIconAlert(player, "배수 &6x" + multiplier + "&f 적용! &6" + payoutAmount + "&f코인을 획득했습니다!");
				} else {
					var loc = player.getLocation();
					Casino.sendIconAlert(player, "매치 실패! 코인을 잃었습니다.");
					player.spawnParticle(Particle.EXPLOSION_EMITTER, loc, 1);
					player.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
				}
			} finally {
				activeRunningMachines.remove(machineId);
			}
		};
	}

	private Symbol drawRandomSymbol() {
		double totalChance = diamondChance + goldChance + silverChance + noLuckChance;
		double rand = ThreadLocalRandom.current().nextDouble() * totalChance;

		if (rand < diamondChance) return Symbol.DIAMOND;
		rand -= diamondChance;
		if (rand < goldChance) return Symbol.GOLD;
		rand -= goldChance;
		if (rand < silverChance) return Symbol.SILVER;

		return Symbol.NO_LUCK;
	}

	private void consumeCoins(Player player, int amount) {
		int left = amount;
		var inv = player.getInventory();
		for (int i = 0; i < inv.getSize(); i++) {
			var item = inv.getItem(i);
			if (Casino.isCoin(item)) {
				if (item.getAmount() <= left) {
					left -= item.getAmount();
					inv.setItem(i, null);
				} else {
					item.setAmount(item.getAmount() - left);
					break;
				}
			}
			if (left <= 0) break;
		}
	}
}
