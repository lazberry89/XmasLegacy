package org.lazberry.xmaslegacy.RoleSwitch;

import io.papermc.paper.math.Rotation;
import io.th0rgal.oraxen.api.OraxenItems;
import lombok.Data;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;

@Data
@Registry.Include(type = ServerType.MAIN)
public class MagicBook {
	public static final NamespacedKey key = KeyUtils.get("book");
    private @Nullable ItemDisplay display;
	private @Nullable Interaction interaction;

	public MagicBook() {}

	@Contract(pure = true)
    private @NotNull ItemStack magicBook() {
        if (OraxenItems.exists(Constants.SELECT_BOOK)) {
            return OraxenItems.getItemById(Constants.SELECT_BOOK).build();
        }
        return new ItemStack(Material.BARRIER);
    }

	public boolean exists() {
		return display != null && interaction != null;
	}

    public ItemDisplay BookStand(@NotNull Location loc) {
        return loc.getWorld().spawn(loc.clone().add(0.5, 1.5, 0.5).setRotation(Rotation.rotation(90, 0)), ItemDisplay.class, i -> {
            i.setItemStack(magicBook());
            i.setBrightness(new Display.Brightness(8, 8));
            Transformation tr = i.getTransformation();
            tr.getScale().set(1.3f, 1.3f, 1.3f);

            i.getPersistentDataContainer().set(key, PersistentDataType.STRING, "rpgbook");
            i.customName(ColorUtils.chat("&c&k#####"));
            i.setCustomNameVisible(true);
	        GlowUtils.glow(i, NamedTextColor.RED);
        });
    }

	public void spawn(Location loc) {
		BookStand(loc);
		loc.getWorld().spawn(loc.clone().add(0.5, 0.5, 0.5), Interaction.class, i -> {
			i.setResponsive(true);
			i.setInteractionWidth(0.8f);
			i.setInteractionHeight(1.2f);
			KeyUtils.set(i, key, "rpgbook");
		});
	}

    public void remove() {
		if (display != null) {
			display.remove();
			display = null;
		}
		if (interaction != null) {
			interaction.remove();
			interaction = null;
		}
    }
}
