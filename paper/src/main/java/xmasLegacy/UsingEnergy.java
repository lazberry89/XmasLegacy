package xmasLegacy;

import org.bukkit.entity.Player;

@SuppressWarnings("DuplicatedCode")
public interface UsingEnergy {
	boolean consumeEnergy(Player p, int hungerCost);
	void useDash(Player p);
}
