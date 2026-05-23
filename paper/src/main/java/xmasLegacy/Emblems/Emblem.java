package xmasLegacy.Emblems;

import io.th0rgal.oraxen.api.OraxenItems;
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
import xmasLegacy.XmasLegacy;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("DuplicatedCode, unused")
public class Emblem {
	private final @NotNull ItemStack TargetEmblem;
	private final @NotNull ItemStack RangeEmblem;
	private final @NotNull Role role;
	private final @NotNull EmblemType type;
	private final XmasLegacy plugin;

	public Emblem(@NotNull Role role, @NotNull EmblemType type) {
		this.role = role;
		this.type = type;
		this.plugin = XmasLegacy.getInstance();
		TargetEmblem = targetEmblem();
		RangeEmblem = rangeEmblem();
	}

	private @NotNull ItemStack targetEmblem() {
		ItemStack target = OraxenItems.getItemById(Constants.TARGET_EMBLEM).build();
		if (target == null) return new ItemStack(Material.BARRIER);
		target.editMeta(meta -> {
			meta.displayName(ColorUtils.chat("&#FF0000T&#FF1515a&#FF2A2Ar&#FF3F3Fg&#FE5454e&#FE6969t &#FE9494E&#FEA9A9m&#FEBEBEb&#FDD3D3l&#FDE8E8e&#FDFDFDm"));
			List<Component> lore = new ArrayList<>(List.of(ColorUtils.chat("&f현재직업 : " + role.getKor()), ColorUtils.chat("&f귀속능력 : &6" + role.bindTarget().getKor())));
			meta.lore(lore);
			meta.getPersistentDataContainer().set(plugin.getNamespacedKey("emblem_type"), PersistentDataType.STRING, "target");
			meta.getPersistentDataContainer().set(plugin.getNamespacedKey("emblem_role"), PersistentDataType.STRING, role.name());
			meta.setRarity(ItemRarity.EPIC);
		});
		return target;
	}

	private @NotNull ItemStack rangeEmblem() {
		ItemStack target = OraxenItems.getItemById(Constants.RANGE_EMBLEM).build();
		if (target == null) return new ItemStack(Material.BARRIER);
		target.editMeta(meta -> {
			meta.displayName(ColorUtils.chat("&#0E00FFR&#2417FFa&#392EFFn&#4F45FEg&#655CFEe &#908AFEE&#A6A1FEm&#BCB8FEb&#D2CFFDl&#E7E6FDe&#FDFDFDm"));
			List<Component> lore = new ArrayList<>(List.of(ColorUtils.chat("&f현재직업 : &6" + role.getKor()), ColorUtils.chat("&f귀속능력 : &6" + role.rangeTarget())));
			meta.lore(lore);
			meta.getPersistentDataContainer().set(plugin.getNamespacedKey("emblem_type"), PersistentDataType.STRING, "range");
			meta.getPersistentDataContainer().set(plugin.getNamespacedKey("emblem_role"), PersistentDataType.STRING, role.name());
			meta.setRarity(ItemRarity.EPIC);
		});
		return target;
	}

	public void give(@NotNull Player p) {
		p.getInventory().addItem(this.TargetEmblem);
		p.getInventory().addItem(this.RangeEmblem);
	}

	public @NotNull ItemStack getTargetEmblem() {
		return this.TargetEmblem;
	}
	public @NotNull ItemStack getRangeEmblem() {
		return this.RangeEmblem;
	}
	public @NotNull Role getRole() {
		return this.role;
	}
	public @NotNull EmblemType getType() {
		return this.type;
	}
}
