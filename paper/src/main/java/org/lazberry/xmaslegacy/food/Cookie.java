package org.lazberry.xmaslegacy.food;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.XmasLegacy;

public class Cookie {

    public static @NotNull ItemStack cookie(int amount) {
		ItemStack item;
		var oraxen = OraxenItems.getItemById("cookie");
		if (oraxen == null) {item = new ItemStack(Material.COOKIE);} else {
		item = oraxen.build();}

        ItemStack a = ItemBuilder.of(XmasLegacy.getInstance(), item)
                .setName(ColorUtils.chat("&c&l라즈베리 쿠키"))
                .setLore(ColorUtils.chat("&8맛은 있는데,배는 고플걸"))
                .setGlint(true)
                .hideAllFlags()
                .addAttribute(Attribute.LUCK, 0.1, AttributeModifier.Operation.ADD_NUMBER)
                .build();
        a.setAmount(amount);
        return a.clone();
    }
}
