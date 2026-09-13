package org.lazberry.xmaslegacy.casino.games.SlotMachine;

import lombok.Data;
import org.bukkit.Location;
import org.lazberry.xmaslegacy.casino.games.GameMachine;
import org.lazberry.xmaslegacy.casino.logics.Machines;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

import java.util.HashMap;
import java.util.Map;

@Data
@ConsumableClass
public class SlotMachine implements GameMachine {
	private final Map<Symbol, Double> multipliesPerSymbol = new HashMap<>();
    private final String name;
    private final Location slot1;
    private final Location slot2;
    private final Location slot3;
    private final Location triggerLocation;
    private final Machines type;
    private final int neededCoin;
    private Symbol symbol1 = Symbol.NO_LUCK;
    private Symbol symbol2 = Symbol.NO_LUCK;
    private Symbol symbol3 = Symbol.NO_LUCK;

    public SlotMachine(String name, Location slot1, Location slot2, Location slot3, Location triggerLocation,
                       int neededCoin, double multipliesForSilver, double multipliesForGold, double multipliesForDiamond) {
        this.name = name;
        this.slot1 = slot1;
        this.slot2 = slot2;
        this.slot3 = slot3;
        this.triggerLocation = triggerLocation.getBlock().getLocation();
        this.type = Machines.SLOT_MACHINE;
        this.neededCoin = neededCoin;
		this.multipliesPerSymbol.put(Symbol.NO_LUCK, 1.0);
		this.multipliesPerSymbol.put(Symbol.SILVER, multipliesForSilver);
		this.multipliesPerSymbol.put(Symbol.GOLD, multipliesForGold);
		this.multipliesPerSymbol.put(Symbol.DIAMOND, multipliesForDiamond);
    }

	public double getMultipliesPerSymbol(Symbol symbol) {
		return this.multipliesPerSymbol.get(symbol);
	}
}
