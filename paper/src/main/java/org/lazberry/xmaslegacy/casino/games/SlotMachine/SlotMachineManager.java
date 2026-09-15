package org.lazberry.xmaslegacy.casino.games.SlotMachine;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.data.type.Switch;
import org.bukkit.entity.Player;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.casino.Casino;
import org.lazberry.xmaslegacy.casino.games.CoinFlip.CoinFlip;
import org.lazberry.xmaslegacy.settings.Annotation.Inject;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.InfoUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.lazberry.xmaslegacy.casino.Casino.countCoins;

@Data
@Registry.Include(type = ServerType.MAIN)
public class SlotMachineManager {
	private final Map<String, SlotMachine> machines = new ConcurrentHashMap<>();
	private final Map<String, SlotAnimationController> controllers = new ConcurrentHashMap<>();
	private final Set<String> activeRunningMachines = ConcurrentHashMap.newKeySet();
	private final XmasLegacy plugin;

	private double diamondChance;
	private double goldChance;
	private double silverChance;
	private double noLuckChance;

	@Inject
	public SlotMachineManager(XmasLegacy plugin) {
		this.plugin = plugin;
	}

	public Optional<SlotMachine> getSlotMachine(String id) {
		return Optional.ofNullable(machines.get(id));
	}

	public Optional<SlotAnimationController> getMachineController(String id) {
		return Optional.ofNullable(controllers.get(id));
	}

	public Optional<SlotMachine> getMachineByTrigger(Location loc) {
		if (loc == null) return Optional.empty();

		return machines.values().stream()
				.filter(m -> m.getTriggerLocation() != null)
				.filter(m -> Objects.equals(m.getTriggerLocation(), loc.getBlock().getLocation()))
				.findFirst();
	}

	public void createSlotMachine(String id, Location slot1, Location slot2, Location slot3, Location triggerLocation, int neededCoin,
	                              double multipliesForSilver, double multipliesForGold, double multipliesForDiamond) {
		SlotMachine newMachine = new SlotMachine(id, slot1, slot2, slot3, triggerLocation, neededCoin, multipliesForSilver, multipliesForGold, multipliesForDiamond);
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
		Casino.removeCoins(player, neededCoins);
		setLeverPowered(machine.getTriggerLocation(), true);

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
				setLeverPowered(machine.getTriggerLocation(), false);
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

	private void setLeverPowered(Location loc, boolean powered) {
		if (loc == null) return;
		var block = loc.getBlock();
		if (block.getBlockData() instanceof Switch lever) {
			lever.setPowered(powered);
			block.setBlockData(lever);
			loc.getWorld().playSound(loc, Sound.BLOCK_LEVER_CLICK, 0.6f, powered ? 0.5f : 0.7f);
		}
	}
}
