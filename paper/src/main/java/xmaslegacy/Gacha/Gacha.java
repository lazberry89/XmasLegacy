package xmaslegacy.Gacha;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmaslegacy.Annotation.Commands;
import xmaslegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Commands(command = "gacha")
public class Gacha {
	private final ItemStack item;
	private final String key;
	private final GachaGrade grade;
	private @Getter @Setter double chance;
	private final ItemStack showItem;

	public Gacha(@NotNull ItemStack item, @NotNull String key, @NotNull GachaGrade grade, double chance) {
		this.item = Objects.requireNonNull(item, "Item cannot be null!");
		this.key = Objects.requireNonNull(key, "Key cannot be null!");
		this.grade = Objects.requireNonNull(grade, "Grade cannot be null!");
		this.chance = chance;
		this.showItem = createShowItem(item);
	}

	private @NotNull ItemStack createShowItem(@NotNull ItemStack item) {
		ItemStack showTem = item.clone();
		NamespacedKey nameKey = JavaPlugin.getPlugin(XmasLegacy.class).getNamespacedKey("gacha");
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

	public @NotNull ItemStack getItem() {
		return this.item;
	}

	public @NotNull String getKey() {
		return this.key;
	}
	public @NotNull GachaGrade getGrade() {
		return this.grade;
	}
	public @NotNull ItemStack getShowItem() {
		return this.showItem;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Gacha c)) return false;
		return Objects.equals(c.getKey(), key);
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}
}
