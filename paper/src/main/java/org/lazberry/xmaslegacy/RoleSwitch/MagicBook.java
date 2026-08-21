package org.lazberry.xmaslegacy.RoleSwitch;

import io.papermc.paper.math.Rotation;
import io.th0rgal.oraxen.api.OraxenItems;
import lombok.Data;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.utils.GlowUtils;
import org.lazberry.xmaslegacy.utils.KeyUtils;
import org.lazberry.xmaslegacy.settings.Annotation.Registry;
import org.lazberry.xmaslegacy.settings.ServerType;

@Data
@Registry.Include(type = ServerType.MAIN)
public class MagicBook {
	private final NamespacedKey key;
    private @Nullable ItemDisplay display;
	private @Nullable Slime slime;

	public MagicBook() {
		key = KeyUtils.get("book");
	}

	@Contract(pure = true)
    private @NotNull ItemStack magicBook() {
        if (OraxenItems.exists(Constants.SELECT_BOOK)) {
            return OraxenItems.getItemById(Constants.SELECT_BOOK).build();
        }
        return new ItemStack(Material.BARRIER);
    }

	public boolean exists() {
		return display != null && slime != null;
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
		loc.getWorld().spawn(loc.clone().add(0.5, 0.5, 0.5), Slime.class, i -> {
			i.setInvisible(true);
			i.setSize(2);
			i.setAI(false);
			i.setSilent(true);
			KeyUtils.set(i, key, "rpgbook");
		});
	}

    public void remove() {
		if (display != null) {
			display.remove();
			display = null;
		}
		if (slime != null) {
			slime.remove();
			slime = null;
		}
    }
}
