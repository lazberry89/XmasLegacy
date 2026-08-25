package org.lazberry.xmaslegacy.gacha;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@ConsumableClass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Gacha {
	private final @NotNull @Getter ItemStack item;
	@EqualsAndHashCode.Include
	private final @NotNull @Getter String key;
	private final @NotNull @Getter GachaGrade grade;
	private @Getter @Setter double chance;
	private final @NotNull @Getter ItemStack showItem;

	public Gacha(@NotNull ItemStack item, @NotNull String key, @NotNull GachaGrade grade, double chance) {
		this.item = Objects.requireNonNull(item, "Item cannot be null!");
		this.key = Objects.requireNonNull(key, "Key cannot be null!");
		this.grade = Objects.requireNonNull(grade, "Grade cannot be null!");
		this.chance = chance;
		this.showItem = createShowItem(item);
	}

	private @NotNull ItemStack createShowItem(@NotNull ItemStack item) {
		ItemStack showTem = item.clone();
		NamespacedKey nameKey = KeyUtils.get("gacha");
		List<Component> lore = new ArrayList<>();
		lore.add(ColorUtils.chat("&6&lGRADE&f&l " + grade.name()));
		lore.add(ColorUtils.chat(String.format("&6&lCHANCE&f&l %.4f", chance)));

		showTem.editMeta(meta -> {
			meta.displayName(ColorUtils.chat(key));
			meta.lore(lore);
			PersistentDataContainer container = meta.getPersistentDataContainer();
			container.set(nameKey, PersistentDataType.STRING, key);
		});

		return showTem;
	}
}
