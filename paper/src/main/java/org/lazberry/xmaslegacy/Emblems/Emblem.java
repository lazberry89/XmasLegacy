package org.lazberry.xmaslegacy.Emblems;

import io.th0rgal.oraxen.api.OraxenItems;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.settings.Annotation.ConsumableClass;
import org.lazberry.xmaslegacy.settings.SkillSet;
import org.lazberry.xmaslegacy.Utils.KeyUtils;

import java.util.ArrayList;
import java.util.List;

@ConsumableClass
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Emblem {
	private final @NotNull ItemStack targetEmblem;
	private final @NotNull ItemStack rangeEmblem;
	@EqualsAndHashCode.Include
	private final @Getter @NotNull Role role;

	public Emblem(@NotNull Role role) {
		this.role = role;
		this.targetEmblem = targetEmblem();
		this.rangeEmblem = rangeEmblem();
	}

	private @NotNull ItemStack targetEmblem() {
		var targetB = OraxenItems.getItemById(Constants.TARGET_EMBLEM);
		if (targetB == null) return new ItemStack(Material.BARRIER);
		ItemStack target = targetB.build();
		if (target == null) return new ItemStack(Material.BARRIER);
		target.editMeta(meta -> {
			meta.displayName(ColorUtils.chat("&cTarget Emblem"));
			List<Component> lore = new ArrayList<>(List.of(ColorUtils.chat("&f현재직업 : &6" + role.getKor()), ColorUtils.chat("&f귀속능력 : &6" + role.bindTarget().getKor())));
			meta.lore(lore);
			meta.getPersistentDataContainer().set(KeyUtils.get("emblem_type"), PersistentDataType.STRING, "target");
			meta.getPersistentDataContainer().set(KeyUtils.get("emblem_role"), PersistentDataType.STRING, role.name());
		});
		return target;
	}

	private @NotNull ItemStack rangeEmblem() {
		var targetB = OraxenItems.getItemById(Constants.RANGE_EMBLEM);
		if (targetB == null) return new ItemStack(Material.BARRIER);
		ItemStack target = targetB.build();
		if (target == null) return new ItemStack(Material.BARRIER);
		target.editMeta(meta -> {
			meta.displayName(ColorUtils.chat("&#0E00FFR&#2417FFa&#392EFFn&#4F45FEg&#655CFEe &#908AFEE&#A6A1FEm&#BCB8FEb&#D2CFFDl&#E7E6FDe&#FDFDFDm"));
			String skillListString = role.bindRange().stream()
					.map(SkillSet::getKor)
					.collect(java.util.stream.Collectors.joining(", "));
			List<Component> lore = new ArrayList<>(List.of(ColorUtils.chat("&f현재직업 : &6" + role.getKor()), ColorUtils.chat("&f귀속능력 : &6" + skillListString)));
			meta.lore(lore);
			meta.getPersistentDataContainer().set(KeyUtils.get("emblem_type"), PersistentDataType.STRING, "range");
			meta.getPersistentDataContainer().set(KeyUtils.get("emblem_role"), PersistentDataType.STRING, role.name());
		});
		return target;
	}

	public void give(@NotNull Player p) {
		p.getInventory().addItem(this.targetEmblem);
		p.getInventory().addItem(this.rangeEmblem);
	}

	public @NotNull ItemStack getTargetEmblem() {
		return this.targetEmblem;
	}
	public @NotNull ItemStack getRangeEmblem() {
		return this.rangeEmblem;
	}
}
