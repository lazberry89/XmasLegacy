package xmasLegacy.Enchant;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.ColorUtils;
import xmasLegacy.Utils.ItemBuilder;
import xmasLegacy.XmasLegacy;

import java.util.List;

public class EnchantMaterial {

    public EnchantMaterial() {}

	public static ItemStack PrismFractal() {
		var oraxen = OraxenItems.getItemById("prism_fractal");

		if (oraxen == null) {
			return ItemBuilder.of(XmasLegacy.getInstance(), Material.PAPER)
					.setName(ColorUtils.chat("&b&l프리즘 조각"))
					.setLore(ColorUtils.chat("&7이 세계의 힘을 가진 아이템이다."),
							ColorUtils.chat("&7장비강화에 사용하여 성장할 수 있으며"),
							ColorUtils.chat("&c이 힘에 침식되지 아니하여야 한다."))
					.setItemModel("")
					.hideAllFlags()
					.setTag("enchant_material", "prism_fractal")
					.setGlint(true)
					.addAttribute(Attribute.MOVEMENT_SPEED, -0.1, AttributeModifier.Operation.ADD_NUMBER)
					.addAttribute(Attribute.CAMERA_DISTANCE, -0.1, AttributeModifier.Operation.ADD_NUMBER)
					.build().clone();
		}

		ItemStack item = oraxen.build();
		item.editMeta(meta -> {
			meta.displayName(ColorUtils.chat("&b&l프리즘 조각"));
			meta.lore(List.of(
					ColorUtils.chat("&7이 세계의 힘을 가진 아이템이다."),
					ColorUtils.chat("&7장비강화에 사용하여 성장할 수 있으며"),
					ColorUtils.chat("&7이 힘에 침식되지 아니하여야 한다.")
			));

			for (ItemFlag flag : ItemFlag.values()) {
				meta.addItemFlags(flag);
			}

			meta.setEnchantmentGlintOverride(true);

			NamespacedKey tagKey = new NamespacedKey(XmasLegacy.getInstance(), "enchant_material");
			meta.getPersistentDataContainer().set(tagKey, PersistentDataType.STRING, "prism_fractal");

			NamespacedKey speedKey = new NamespacedKey(XmasLegacy.getInstance(), Attribute.MOVEMENT_SPEED.getKey().getKey());
			AttributeModifier speedModifier = new AttributeModifier(speedKey, -0.1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND);
			meta.addAttributeModifier(Attribute.MOVEMENT_SPEED, speedModifier);

			NamespacedKey cameraKey = new NamespacedKey(XmasLegacy.getInstance(), Attribute.CAMERA_DISTANCE.getKey().getKey());
			AttributeModifier cameraModifier = new AttributeModifier(cameraKey, -0.1, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND);
			meta.addAttributeModifier(Attribute.CAMERA_DISTANCE, cameraModifier);
		});

		return item;
	}

	public static boolean isMaterial(ItemStack item) {
		if (item.isSimilar(PrismFractal())) return true;

		ItemMeta meta = item.getItemMeta();
		if (meta == null) return false;

		if (!meta.getPersistentDataContainer().has(XmasLegacy.getInstance().getNamespacedKey("enchant_material"), PersistentDataType.STRING)) return false;
		String type = meta.getPersistentDataContainer().get(XmasLegacy.getInstance().getNamespacedKey("enchant_material"), PersistentDataType.STRING);
		return type != null && type.equalsIgnoreCase("prism_fractal");
	}
}
