package org.lazberry.xmaslegacy.casino.games.SlotMachine;

import lombok.Data;
import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.framework.annotation.ConsumableClass;

import java.util.HashMap;
import java.util.Map;

@Data
@ConsumableClass
public class SlotMachine {
	private final Map<Symbol, Double> multipliesPerSymbol = new HashMap<>();
    private final String name;
    private final Location triggerLocation;
    private final int neededCoin;
	/** The three reel positions; set one by one with /casino slot reel, so they start out empty. */
	private final Location[] reels = new Location[3];

    private Symbol symbol1 = Symbol.NO_LUCK;
    private Symbol symbol2 = Symbol.NO_LUCK;
    private Symbol symbol3 = Symbol.NO_LUCK;

    public SlotMachine(String name, Location triggerLocation,
                       int neededCoin, double multipliesForSilver, double multipliesForGold, double multipliesForDiamond) {
        this.name = name;
        this.triggerLocation = triggerLocation.getBlock().getLocation();
        this.neededCoin = neededCoin;

		this.multipliesPerSymbol.put(Symbol.NO_LUCK, 1.0);
		this.multipliesPerSymbol.put(Symbol.SILVER, multipliesForSilver);
		this.multipliesPerSymbol.put(Symbol.GOLD, multipliesForGold);
		this.multipliesPerSymbol.put(Symbol.DIAMOND, multipliesForDiamond);
    }

	public double getMultipliesPerSymbol(Symbol symbol) {
		return this.multipliesPerSymbol.get(symbol);
	}

	/** @param index 1, 2 or 3 */
	public void setReel(int index, @Nullable Location location) {
		reels[index - 1] = location == null ? null : location.getBlock().getLocation();
	}

	public @Nullable Location getSlot1() { return reels[0]; }
	public @Nullable Location getSlot2() { return reels[1]; }
	public @Nullable Location getSlot3() { return reels[2]; }

	public boolean isReady() {
		return reels[0] != null && reels[1] != null && reels[2] != null;
	}
}
