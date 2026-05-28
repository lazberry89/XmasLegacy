package xmasLegacy.Enchant;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

public class EnchantMaterial {

    public EnchantMaterial() {}

    public static ItemStack PrismFractal() {
        return ItemBuilder.of(XmasLegacy.getInstance(), Material.PAPER)
                .setName(ColorUtils.chat("&b&l프리즘 조각"))
                .setLore(ColorUtils.chat("&7이 세계의 힘을 가진 아이템이다."),
                        ColorUtils.chat("&7장비강화에 사용하여 성장할 수 있으며"),
                        ColorUtils.chat("&7이 힘에 침식되지 아니하여야 한다."))
                .setItemModel("")
                .hideAllFlags()
                .setTag("enchant_material", "prism_fractal")
                .setGlint(true)
                .addAttribute(Attribute.MOVEMENT_SPEED, -0.1, AttributeModifier.Operation.ADD_NUMBER)
                .addAttribute(Attribute.CAMERA_DISTANCE, -0.1, AttributeModifier.Operation.ADD_NUMBER)
                .build().clone();
    }
}
