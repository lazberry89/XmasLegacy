package org.lazberry.xmaslegacy.airdrop;

import org.bukkit.inventory.ItemStack;

public record AirdropContents(
		ItemStack item,
		int minimumAmount,
		int maximumAmount,
		double chancePerSlot
) {
}
