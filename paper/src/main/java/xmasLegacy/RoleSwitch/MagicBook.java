package xmasLegacy.RoleSwitch;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
    private ArmorStand stand;

    public MagicBook(XmasLegacy plugin) {
        this.plugin = plugin;
        this.magicBook = magicBook();
        this.um = plugin.UM;
    }

    @Contract(pure = true)
    private @NotNull ItemStack magicBook() {
        if (OraxenItems.exists(Constants.SELECT_BOOK)) {
            return OraxenItems.getItemById(Constants.SELECT_BOOK).build();
        }
        return new ItemStack(Material.BOOK);
    }

    public void openRoleSelection(@NotNull Player p) {
        User user = um.getUser(p.getUniqueId());
        if (user == null) return;
        if (!Roles.USER.equals(user.getRole())) {
            plugin.infoMsg(InfoLevel.ERROR, p, "이미 직업이 선택되었어요.");
            return;
        }
        p.openInventory(new RoleSelectInterface(plugin).getInventory());
    }

    @CanIgnoreReturnValue
    public ArmorStand BookStand(@NotNull Location loc) {
        NamespacedKey key = plugin.getNamespacedKey("book");
        return loc.getWorld().spawn(loc, ArmorStand.class, s -> {
           s.setCollidable(false);
           s.setInvisible(true);
           s.setInvulnerable(true);

           double tilt = Math.toRadians(25);
           s.setHeadPose(new org.bukkit.util.EulerAngle(tilt, 0, 0));
           s.getEquipment().setHelmet(this.magicBook);

           s.getPersistentDataContainer().set(key, PersistentDataType.STRING, "rpgbook");
           s.setBasePlate(false);
        });
    }

    public void setStand(ArmorStand stand) {
        this.stand = stand;
    }

    public @Nullable ArmorStand getStand() {
        return this.stand;
    }
}
