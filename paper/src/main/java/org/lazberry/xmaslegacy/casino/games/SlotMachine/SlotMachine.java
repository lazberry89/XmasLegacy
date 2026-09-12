package org.lazberry.xmaslegacy.casino.games.SlotMachine;

import lombok.Data;
import org.bukkit.Location;
import org.lazberry.xmaslegacy.casino.games.GameMachine;
import org.lazberry.xmaslegacy.casino.logics.Machines;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

@Data
@ConsumableClass
public class SlotMachine implements GameMachine {
    private final String name;
    private final Location slot1;
    private final Location slot2;
    private final Location slot3;
    private final Machines type;
    private final int amount;
    private final double multiplies;
    private Symbol symbol1 = Symbol.NO_LUCK;
    private Symbol symbol2 = Symbol.NO_LUCK;
    private Symbol symbol3 = Symbol.NO_LUCK;

    public SlotMachine(String name, Location slot1, Location slot2, Location slot3, Machines type, int amount, double multiplies) {
        this.name = name;
        this.slot1 = slot1;
        this.slot2 = slot2;
        this.slot3 = slot3;
        this.type = type;
        this.amount = amount;
        this.multiplies = multiplies;
    }
}
