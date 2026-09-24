package org.lazberry.xmaslegacy.blueprint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.lazberry.xmaslegacy.utils.ColorUtils;

@Getter
@RequiredArgsConstructor
public enum BlueprintGrade {
	LOW(ColorUtils.chat("&7&l[ 하급 도면 ]"), Material.IRON_BLOCK, 0, 1_000_000),
	MEDIUM(ColorUtils.chat("&6&l[ 중급 도면 ]"), Material.GOLD_BLOCK, 1_000_000, 2_500_000),
	HIGH(ColorUtils.chat("&b&l[ 상급 도면 ]"), Material.DIAMOND_BLOCK, 2_500_000, Integer.MAX_VALUE);

	private final Component displayName;
	private final Material icon;
	private final int minPrice;
	private final int maxPrice;

	public static BlueprintGrade fromPrice(int price) {
		if (price < 1_000_000) return LOW;
		if (price < 2_500_000) return MEDIUM;
		return HIGH;
	}
}
