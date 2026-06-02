package xmasLegacy.RoleSwitch;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.ColorUtils;
import org.lazberry.xmaslegacy.Constants;
import org.lazberry.xmaslegacy.Roles.Roles;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import xmasLegacy.InfoLevel;
import xmasLegacy.RoleSelection.RoleSelectInterface;
import xmasLegacy.XmasLegacy;

public class MagicBook {
    private final ItemStack magicBook;
    private final XmasLegacy plugin;
    private final UserManager um;
    private ItemDisplay display;
    private static MagicBook instance;

    public static MagicBook getInstance() {
        if (instance == null) instance = new MagicBook();
        return instance;
    }

    private MagicBook() {
        this.plugin = XmasLegacy.getInstance();
        this.magicBook = magicBook();
        this.um = UserManager.getInstance();
    }

    @Contract(pure = true)
    private @NotNull ItemStack magicBook() {
        if (OraxenItems.exists(Constants.SELECT_BOOK)) {
            return OraxenItems.getItemById(Constants.SELECT_BOOK).build();
        }
        return new ItemStack(Material.BARRIER);
    }

    public void openRoleSelection(@NotNull Player p) {
        User user = um.getUser(p.getUniqueId());
        if (user == null) return;
        if (!Roles.USER.equals(user.getRole())) {
            plugin.infoMsg(InfoLevel.ERROR, p, "이미 직업이 선택되었어요.");
            return;
        }
        p.openInventory(new RoleSelectInterface().getInventory());
    }

    public void deleteStand() {
        if (display == null) return;
        display.remove();
        display = null;
    }

    @CanIgnoreReturnValue
    public ItemDisplay BookStand(@NotNull Location loc) {
        NamespacedKey key = plugin.getNamespacedKey("book");
        return loc.getWorld().spawn(loc.clone().add(0, 1, 0), ItemDisplay.class, i -> {
            i.setItemStack(magicBook());
            i.setBrightness(new Display.Brightness(8, 8));
            Transformation tr = i.getTransformation();
            tr.getScale().set(1.3f, 1.3f, 1.3f);

            i.getPersistentDataContainer().set(key, PersistentDataType.STRING, "rpgbook");
            i.customName(ColorUtils.chat("&c&k#####"));
            i.setCustomNameVisible(true);
        });
    }

    public void setDisplay(ItemDisplay display) {
        this.display = display;
    }

    public @Nullable ItemDisplay getStand() {
        return this.display;
    }
}
