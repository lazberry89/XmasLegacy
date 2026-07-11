package org.lazberry.xmaslegacy.RoleManagers;

import lombok.extern.slf4j.Slf4j;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.Annotation.Listeners;
import org.lazberry.xmaslegacy.Roles.BasicRoles;
import org.lazberry.xmaslegacy.Roles.Role;
import org.lazberry.xmaslegacy.Roles.SecondaryRoles;
import org.lazberry.xmaslegacy.Utils.KeyUtils;

@Slf4j
@Listeners
public class RoleListener implements Listener {
    private final @NotNull NamespacedKey key;
    private final @NotNull RoleManager rlm;

    public RoleListener() {
        this.key = KeyUtils.get("role_id");
        this.rlm = RoleManager.INSTANCE;
    }

    @EventHandler
    public void UsingDash(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        if (p.isSneaking()) return;

        ItemStack tool = p.getInventory().getItemInMainHand();
        if (!(KeyUtils.hasKey(tool, this.key))) return;

        String value = KeyUtils.get(tool, key, BasicRoles.USER.name());
        Role role;
        try {
            role = Role.valueOf(value);
        } catch (IllegalArgumentException ex) {
            log.warn("Failed to parse role named \"{}\"", value);
            role = BasicRoles.USER;
        }
        RoleClass rc = RoleManager.INSTANCE.getRoleInstance(role);
        rc.useDash(p, role);
    }

    @EventHandler
    public void SkillUse(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack tool = p.getInventory().getItemInMainHand();
        if (tool.getType().isAir()) return;
        PersistentDataContainer container = tool.getItemMeta().getPersistentDataContainer();
        String type = container.get(KeyUtils.get("emblem_type"), PersistentDataType.STRING);
        String roleS = container.get(KeyUtils.get("emblem_role"), PersistentDataType.STRING);
        if (type == null || roleS == null) return;
        Role role;
        try {
            role = Role.valueOf(roleS);
        } catch (IllegalArgumentException ex) {
            log.error("Role method 'valueOf(String name)' invoked error. -> \"{}\"", roleS);
            role = null;
        }
        if (role == null) return;
        switch (type) {
            case "target" -> rlm.getRoleInstance(role).useFirstSkill(p);
            case "range" -> rlm.getRoleInstance(role).useSecondSkill(p);
            default -> log.error("Emblem type mismatch. Type: {}", type);
        }
    }
}
