package org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.SkillListeners;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.User.User;
import org.lazberry.xmaslegacy.User.UserManager;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.AbstractFirstRole;
import org.lazberry.xmaslegacy.RoleManagers.FirstRoleManager.FirstRoleManager;
import org.lazberry.xmaslegacy.Annotation.Listeners;
import org.lazberry.xmaslegacy.Utils.KeyUtils;
import org.lazberry.xmaslegacy.XmasLegacy;

@SuppressWarnings("unused, FieldCanBeLocal, DuplicatedCode")
@Listeners
public class FirstRoleListener implements Listener {
	private final XmasLegacy plugin;
	private final FirstRoleManager frm;
    private final UserManager um;

	public FirstRoleListener() {
		this.plugin = XmasLegacy.getInstance();
		this.frm = FirstRoleManager.INSTANCE;
        this.um = UserManager.INSTANCE;
	}

    @EventHandler
    public void useDash(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        if (item.getType().isAir()) return;

        if (!e.getAction().isRightClick()) return;

        User user = um.getUser(p.getUniqueId());
        if (user == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        String value = container.get(KeyUtils.get("role_id"), PersistentDataType.STRING);
        if (value == null) return;
        Role role;
        try {
            role = Role.valueOf(value);
        } catch(IllegalArgumentException ex) {
            plugin.getSLF4JLogger().error("Could not find Role \"{}\"", value, ex);
            role = BasicRoles.USER;
        }
        if (role instanceof BasicRoles fr) {
            AbstractFirstRole afr = frm.getRoleInstance(fr);
			if (afr == null) return;
            afr.useDash(p, role);
        }
    }

    //Archer skill
    @EventHandler
    public void onShockHit(ProjectileHitEvent e) {
        NamespacedKey key = KeyUtils.get("skill");
        String npKey = e.getEntity().getPersistentDataContainer().get(key, PersistentDataType.STRING);
        Projectile projectile = e.getEntity();
        if (!(projectile.getShooter() instanceof Player) && !(projectile instanceof Arrow a)) return;
        if (!(npKey != null && npKey.equals("archer_arrow"))) return;

        if (e.getHitBlock() != null) {
            Location loc = e.getHitBlock().getLocation();
            loc.getWorld().strikeLightning(loc);
            return;
        }

        if (e.getHitEntity() instanceof LivingEntity victim) {
            Location loc = victim.getLocation();
	        loc.getWorld().strikeLightning(loc);
        }
    }
}
